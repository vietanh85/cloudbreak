package com.sequenceiq.it.cloudbreak;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.inject.Inject;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.sequenceiq.cloudbreak.api.model.DatabaseVendor;
import com.sequenceiq.cloudbreak.api.model.RecipeType;
import com.sequenceiq.cloudbreak.api.model.rds.RdsType;
import com.sequenceiq.cloudbreak.api.model.stack.cluster.gateway.SSOType;
import com.sequenceiq.cloudbreak.blueprint.template.views.RdsView;
import com.sequenceiq.cloudbreak.cloud.model.AmbariRepo;
import com.sequenceiq.cloudbreak.concurrent.MDCCleanerTaskDecorator;
import com.sequenceiq.cloudbreak.orchestrator.exception.CloudbreakOrchestratorException;
import com.sequenceiq.cloudbreak.orchestrator.exception.CloudbreakOrchestratorFailedException;
import com.sequenceiq.cloudbreak.orchestrator.executor.ParallelOrchestratorComponentRunner;
import com.sequenceiq.cloudbreak.orchestrator.model.BootstrapParams;
import com.sequenceiq.cloudbreak.orchestrator.model.GatewayConfig;
import com.sequenceiq.cloudbreak.orchestrator.model.Node;
import com.sequenceiq.cloudbreak.orchestrator.model.RecipeModel;
import com.sequenceiq.cloudbreak.orchestrator.model.SaltConfig;
import com.sequenceiq.cloudbreak.orchestrator.model.SaltPillarProperties;
import com.sequenceiq.cloudbreak.orchestrator.salt.SaltOrchestrator;
import com.sequenceiq.cloudbreak.orchestrator.state.ExitCriteria;
import com.sequenceiq.cloudbreak.orchestrator.state.ExitCriteriaModel;

@ContextConfiguration
@SpringBootTest
@TestPropertySource(properties = {
        "rest.debug=true",
})
public class ImageTests extends AbstractTestNGSpringContextTests {

    public static final String SCRIPT = "#!/bin/bash -e\n"
            + "\n"
            + "create_user_home() {\n"
            + "  export DOMAIN=$(dnsdomainname)\n"
            + "su hdfs<<EOF\n"
            + "  if [ -d /etc/security/keytabs ]; then\n"
            + "    echo \"kinit using realm: ${DOMAIN^^}\"\n"
            + "  \tkinit -V -kt /etc/security/keytabs/dn.service.keytab dn/$(hostname -f)@${DOMAIN^^}\n"
            + "  fi\n"
            + "\n"
            + "  if ! hadoop fs -ls /user/$1 2> /dev/null; then\n"
            + "    hadoop fs -mkdir /user/$1 2> /dev/null\n"
            + "    hadoop fs -chown $1:hdfs /user/$1 2> /dev/null\n"
            + "    hadoop dfsadmin -refreshUserToGroupsMappings\n"
            + "    echo \"created /user/$1\"\n"
            + "  else\n"
            + "    echo \"/user/$1 already exists, skipping...\"\n"
            + "  fi\n"
            + "EOF\n"
            + "}\n"
            + "\n"
            + "main(){\n"
            + "  create_user_home yarn\n"
            + "  create_user_home admin\n"
            + "}\n"
            + "\n"
            + "[[ \"$0\" == \"$BASH_SOURCE\" ]] && main \"$@\"\n";

    public static final String AMBARI_DB_PASSWORD = "119mebph99j2bcau2hkrhisqea";

    public static final String HIVE_DB_PASSWORD = "1u43n29lm3ll9me196u78avp3o";

    public static final String AWS_PLATFORM = "AWS";

    public static final String HOST_GROUP = "master";

    public static final String REPO_ID = "HDP-2.6";

    public static final String HDP_VERSION = "2.6.5.0-292";

    public static final String REDHAT_HDP_REPO_URL = "http://public-repo-1.hortonworks.com/HDP/centos6/2.x/updates/2.6.5.0";

    public static final String REDHAT_HDP_VDF_URL = "http://public-repo-1.hortonworks.com/HDP/centos6/2.x/updates/2.6.5.0/HDP-2.6.5.0-292.xml";

    public static final String HIVE_DB = "hive";

    public static final String HIVE_USER = "hive";

    public static final String AMBARI_DB = "ambari";

    public static final String AMBARI_USER = "ambari";

    public static final String REDHAT_AMBARI_REPO_URL = "http://public-repo-1.hortonworks.com/ambari/centos6/2.x/updates/2.6.2.0";

    public static final String AMBARI_VERSION = "2.6.2.0";

    public static final ExitCriteria EXIT_CRITERIA = new ExitCriteria() {
        @Override
        public boolean isExitNeeded(ExitCriteriaModel exitCriteriaModel) {
            return false;
        }

        @Override
        public String exitMessage() {
            return "No more questions";
        }
    };

    private String connectionAddress = "54.72.155.142";

    private String publicAddress = connectionAddress;

    private String privateAddress = "10.0.31.41";

    private String hostname = "ip-10-0-31-41.eu-west-1.compute.internal";

    private GatewayConfig gatewayConfig;

    private Node node;

    private ExitCriteriaModel exitModel;

    @Inject
    private SaltOrchestrator saltOrchestratorUnderTest;

    public ImageTests() {

        Integer gatewayPort = 9443;
        String saltPassword = "1m2r2de5vt3t3c13s3dkp5hi35";
        String saltBootPassword = "4mn16ia3a1lucen3igb404h2v2";
        String signatureKey = "-----BEGIN RSA PRIVATE KEY-----\n"
                + "MIIEogIBAAKCAQEAlT9Oe9YFWEXAGv/PtRfFxh7RwF73bjvRLLVvRekd3Cp1zTKj\n"
                + "GlRsgGWqCjFpG87KKop2ThTc4hiLKuum6ZFzqgOwo6lUTHqoM7kVsgYf1pS+JmyP\n"
                + "peUI8lQofTj5ZI5dvpohBS+1ku/5kEaq3Kev9eaapekV23hJcWfuOdrh4CB7EuAM\n"
                + "gI2F80VLL6L40PB22EWvR/sahTXRNrEwp/4/AZpqbrNu5z5v4F0d4aMTB5KrTAAj\n"
                + "WGN3GPyfUuTCOk2Szdc5Li4H/P5hjl28fxtHJkOxfr9H+79BAQpPwejY2bbgbfSm\n"
                + "DP6xBO/MkXRfy8M1WJAGvLi32VOOefCCO9CvfQIDAQABAoIBAE6RrjqJF/U5ewF7\n"
                + "rBf0tNwwu1FfbTit6tdziGmwaFTQUYW945ln2lrZqmOFUYUfHtS4YcGHQCUSsKje\n"
                + "t9b8CHz3RlVSPhuo8sWP6Tj3+mjXkybdPd4ircwMu+f4R2pFfFpr/3ggfqElDieC\n"
                + "3E4fRQXZ6Y3wH96S+27lPMdeUEzRXXxri20m4yWCv9CztKTwxTveZF2RvGJtf2Ao\n"
                + "cZhyCyGKZsQC+I4QhjHcfSFFgdjbOH6/GAXXMg5/NPYRIOv521jPb7aoPxTDfQTq\n"
                + "Yta3Gdv6HrfxXt80YlBlxHRIqVHizlPXs9CWL/hTLEwS9i0yQCNHXjAakqZ8UXLm\n"
                + "jQnBISECgYEA2sgRn0AnvTN+po3kW2TweIULTo132QqCh8jhIXXYM93c/fIzVIto\n"
                + "xj8Ae8de5gFw2WOd+mkDVqL/Ayd1VVvQ0BmVnsjY8GkTN4JW9IvdxYGqeQewj7kq\n"
                + "uW6m2dQn35XSqwsvXU1j34Fxpl3LfsrchiCMCuH5VUOZYBd0xxUTLgkCgYEArqMF\n"
                + "/UrT8L2JLiE/Tl77afptZ2oSKMqfcm9SAksPe3IODEv9apliMIJzwRVMLIB5Sug7\n"
                + "DOcKh5KSIl/hMoY2XSmyBVGDf8rtRAdx/tujL9YH/V0b3LIf0LxCZ8NvrqrI4PpH\n"
                + "6skxBNO7fmjpYTijNhMiUEi1HDCwN22bJARY0tUCgYAS+mn064bG9dGVtxdJCk+F\n"
                + "1NyvOrxbunqqNrW4xlUz4poJ+VmUiudS0yJCmn/T6eoVIXwRvcxJVqhSaLjX57gM\n"
                + "ZTvCY1/WMvc0yHysh/l76YyVJSKexQw8u6mDmdC/p2p35ed863qvZ7YpHVfKruRE\n"
                + "Skx7oTCUweFrIX3Cy0Q3qQKBgG+9NZ7c2wEldPnEIip2Ea28o3XQ46+f/ieNXM7E\n"
                + "0jItRxnNIWesnqvlPEVl6ChtR66LKwVVa2JVgRVnfcMDJxOzEwneHyKMaAXoMaNd\n"
                + "S2vXKaOOiL5MZpj7bA3Secor0n5JBe0PMEMjass2O2WOhLebxI/UBbX03TuJ8QM5\n"
                + "mM/FAoGAViINhRORRNG7v79Z7Vg+nZD+i2yns/bmWA4bajjy/fg4bDFiS2BaCKxi\n"
                + "dEqARb5t3ryVKP3b5xsPbL7IOyPhrqwz2/Y5yo60YVY8KTLQb0DRRLIpapOUMyTG\n"
                + "8s+ISV/9adTN1XP8xE05aDVd52U+xSkS+Npw6pRHxwu15c/IX6U=\n"
                + "-----END RSA PRIVATE KEY-----\n";
        String serverCert = "-----BEGIN CERTIFICATE-----\n"
                + "MIIDBjCCAfCgAwIBAgIRAPsTQ2WgTK/uX6GX13oRgJMwCwYJKoZIhvcNAQELMBIx\n"
                + "EDAOBgNVBAoTB2dhdGV3YXkwHhcNMTgwNzE1MTcwODAwWhcNMjEwNjI5MTcwODAw\n"
                + "WjASMRAwDgYDVQQKEwdnYXRld2F5MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIB\n"
                + "CgKCAQEAnziZYq84ahPXVX6LjmI5ujgKfJFDq2Xi6gWnmtQCV+T4b86THgY+t5GN\n"
                + "b+jnENtOHliFh50YuUlLz1m6C0tKCp3JZB6Dq3tx7rb57rJmWaECCHsMPmLZBYyh\n"
                + "QYbGlLKrwLuwKQB1RgsRj4FuKkEX9/QFPzvhHmIHCCfBKja78wwb4+WpMgSbJ5e6\n"
                + "GPgZoQjwlWjoW9HlQdDpm8r3iL4mZP/qkg9M3HmVujiwlZ9W4MyKS2YkmVzBE7Gw\n"
                + "fBZrLvTRNMqfwqW4etDT2ap2fUHCE/pYqTFgNvyBjPllyQp0LcyZxJcys6HCIEx2\n"
                + "/jGRf0FyabnoFHODUlpwHPkTAVGUiQIDAQABo1swWTAOBgNVHQ8BAf8EBAMCAKgw\n"
                + "HQYDVR0lBBYwFAYIKwYBBQUHAwIGCCsGAQUFBwMBMAwGA1UdEwEB/wQCMAAwGgYD\n"
                + "VR0RBBMwEYIJbG9jYWxob3N0hwR/AAABMAsGCSqGSIb3DQEBCwOCAQEAJyNszK0P\n"
                + "qqif48OIW5TwVsp9Tn04iOm4Up+TVxuzNOlFWyZ0S8gRMSl/R5wKKUecMvUYWLBP\n"
                + "dKpOK9LvkF54+f1k7UupzK1a2UOxvtp8L3TqJesEvh9nmTCWHV6sK4VtqRnZj5xM\n"
                + "/MuURf6wl1n43Pdq3cQG5sNwJ7l/ZthuLIfq2PJBTaf+2CrmtL14rg8OF1aWhNrl\n"
                + "gSs7KrK8SySqY1aH0br8SNptzPVMrv0mTaVKQh0fwQrNbZ81NGdYWMU0HfNZf2KO\n"
                + "TwCB1RAzeLfeMrUKyaHVyKfyV011DFvKuaYOBa94/q3rtJ3yvqJbhPOGbKL14BFy\n"
                + "D77MWTNRL5G58g==\n"
                + "-----END CERTIFICATE-----\n";
        String clientCert = "-----BEGIN CERTIFICATE-----\n"
                + "MIICozCCAYugAwIBAgIBATANBgkqhkiG9w0BAQsFADAVMRMwEQYDVQQDDApjbG91\n"
                + "ZGJyZWFrMB4XDTE4MDcxNTE3MTEzMVoXDTI4MDcxNTE3MTEzMVowFTETMBEGA1UE\n"
                + "AxMKY2xvdWRicmVhazCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALxw\n"
                + "uW1QM9u/110nnUedEB30YqQh9YQgVnOCCIlLm/rYpcOOKaGZE96lahgqp9YLYcCl\n"
                + "aQboNL4fE96Ba3HZPnaLld4yLyKT1GFz9kuhfETeq2g+pfs05cQAudKj0mFrbF1v\n"
                + "CuHpwwhNLT31ZAqL7aBuffdTReo4I64/I2xKcQzkf/gbEJUENqp626UoNdCZEFJL\n"
                + "p9UyvKP4LG3JczGgV9VCIFqnGZwIdcexpBO2sh3/DYWWuD7BODCjkRDeF/87uyA6\n"
                + "SaHL2aPUMiq8zzoj+sUQbsFBrIf/WpdyFsuPOQ6m1Gt7Jqu3kl43Yz2PoovX0zvS\n"
                + "HTIA1FmDbD32bruXBCECAwEAATANBgkqhkiG9w0BAQsFAAOCAQEAAVGrstHzLeau\n"
                + "ZL5w2lTChGef+WhUEcM/LpB/KV7RjcEMroRe/mpoGNyy0TkmIxfzPKvpZWDm8L7G\n"
                + "LD2l4OCVhyW9mpV/OQ8CTbUl+oqJ4pCxNGYNuvRjdJjZUpxo05xfseQRGsUmFIFr\n"
                + "gVkW1seK3qrdDTDvFE7OMxuFcCWosCsMR4GE3IvU0efdctDp+5z6kVR40tSNRDxv\n"
                + "1R2ecOX/wVucbcjZcymmF3Gwfn6fUblWo+hG/oQg7raC/JuPU0luU6id2YcVRcKl\n"
                + "TI+C1YMdlE0+4pb2W5+7ck5YLGPhf+WubFGQ/oKC2JNzDvCJ2VuNaq0QWBmWagWX\n"
                + "CN/zZAvWZA==\n"
                + "-----END CERTIFICATE-----\n";
        String clientKey = "-----BEGIN RSA PRIVATE KEY-----\n"
                + "MIIEpAIBAAKCAQEAvHC5bVAz27/XXSedR50QHfRipCH1hCBWc4IIiUub+tilw44p\n"
                + "oZkT3qVqGCqn1gthwKVpBug0vh8T3oFrcdk+douV3jIvIpPUYXP2S6F8RN6raD6l\n"
                + "+zTlxAC50qPSYWtsXW8K4enDCE0tPfVkCovtoG5991NF6jgjrj8jbEpxDOR/+BsQ\n"
                + "lQQ2qnrbpSg10JkQUkun1TK8o/gsbclzMaBX1UIgWqcZnAh1x7GkE7ayHf8NhZa4\n"
                + "PsE4MKOREN4X/zu7IDpJocvZo9QyKrzPOiP6xRBuwUGsh/9al3IWy485DqbUa3sm\n"
                + "q7eSXjdjPY+ii9fTO9IdMgDUWYNsPfZuu5cEIQIDAQABAoIBAQCHPyyG3YJq3PS4\n"
                + "ol8K8BqKNUW64bix/PevbYus5rxrvKS0h0sv9YtCSFyuPac6Q+8D8nRABdcdAXck\n"
                + "QqUYEED5mlOVJ1WXLpzG2RDT1XI4h8xkRFqiwqOKGq9EFFpBm3UqVjFsVXeqdKHd\n"
                + "D0ufGjER7VuxcG4EnghV3nERHximk/mazKxsjsRvIGE4Qn9EKaoyJIUKEmoHiEzh\n"
                + "NwQNBlAh/Ayl6qq0Xu/k+N4Y04iI69cDqKxgaQb2mtSKMqhkWxWA8JyQtb6kNqmw\n"
                + "Ss58FcGeoQ63fI0RQlq7WHcxO1dDBxRqUMltXWJZGPWYH8EYTLaAFSwPduhSn2vA\n"
                + "2ufbGB0BAoGBAOroAg0L9yv3o7pLyp9mAZHMyeQ2WNkuIGfvVkE0XRnG+ElS31zj\n"
                + "7g2zZgiGFbDdbs/izMf8NgmENS32CveKQjHxeXHuyG31/xKPZvFBn4XdXb4BvxDe\n"
                + "EIJPVRE+C6eqlRVFJv/KBz59Gu8s3/TaX6InIVjahwYkb386qxOppRRRAoGBAM1c\n"
                + "kEvgScI1z21d5TIxs5H8KpW1qITSEIquPy37v2bfhRQRiBExPqss2nJAaC7XfO+9\n"
                + "YfhD83sWrmggyeSfKwUiz8/s8+m3nG9GUgwx2P866+DqulH2UQctFbAMBF42fH/X\n"
                + "EtZpvfURZUJ7u7zpBbsopQKk7bSINVXfWpG1JQ7RAoGAGVWEQxvzQE6o3cKmy6IY\n"
                + "sTzA2VfzJRkQbaUmTEn1cH5A8Md7R21dkySx8GFlnXmEJe9z3m2Y9lXH1nkenavP\n"
                + "j8tXKa5y90j8hWfp1kMZjTpejctqa8tHRIaByiZC44ZGJdEbb6K+PUMI0JqqXKHg\n"
                + "hoBp3EdVIJh1XedZ4/j70MECgYEAhRSlFealJcwFPdSJQLvgqIwHdLkiGeCfg59j\n"
                + "pgLsiFf1P4oU6T9GY1tPvJZsbV6LwjiyElbGpE+Qej41V4imMk/a592gOycXoyyo\n"
                + "4uoyu0ONtFWopJDA9auL0GgZNIyOpbHUrUodJYE+Y5UXllZTtnOia1JCEKnsFlgF\n"
                + "s0L2XuECgYB2/3BRd7MWUUfZCN92rx9jSkjcL2/gxqd0yMgJNH28dwH1SQkUcttl\n"
                + "snN0uRnDNzLD+a0x01XfO85ov1Lv/FfNhMipQNY50coennsEZ93GCJObNBz10R8j\n"
                + "ziXjWrvpQFPLw03D4AaWAcSduHmsNs4eqZX2HNA/gcmu8y1GhsFvAA==\n"
                + "-----END RSA PRIVATE KEY-----\n";
        String saltSignPrivateKey = "-----BEGIN RSA PRIVATE KEY-----\n"
                + "MIIEowIBAAKCAQEAmx79HpuHsXxjCqpcG0LMCXax0DGatsoMVSPRN7tKhqYG0MBY\n"
                + "cUlkOTBTUnopFcQQEjpWycI7Qi/0oKSGgRA/aomIX9/iJk3k3+IORTpV3WW240n2\n"
                + "fKXt8qu/9wn+cg7SLDaenwLKWoC5D7QSrpjm1o8nFrcJS9EYhnBArys37TrF7hi6\n"
                + "MO6sB1Yb8y1q7ZuQ7V9bQzuH8kK0+umVixP8FpwAD+EZ8Lo1smbTqeopvWXp7aJ6\n"
                + "zte3qrmhifyZRPMzZ6IwagI76q7EaO96cHRqEVXL7TUrCaie4xsSF9X3Vjd2OGix\n"
                + "c6KkQGYEV0YFAYiyU1AdOlqiVw5pw95NdRH/YQIDAQABAoIBAAHwQjOGuAxkxIwW\n"
                + "ku4YSJp5rEEwzYEpRzwtJWkv4v+JxZ4IOVK5GxKH4xKtyiOmfpjrt7fvwHPUS5dS\n"
                + "Vwf33wMC0vx3vMjDwm910L3Wu8n3gnCWtQjbvfyBXvXEXzSnsMKysArGqya0Yxuq\n"
                + "IW2jJYda5J8pDGmxJEBR2M0XmixRwa8/f5i9/7HIwMlMCrV1HoQJPWxnWZ1iyv1Z\n"
                + "bLj9b9CTGo0BHeIpON9qfRJiOMoIbuUT1CK0HGKzjPUbO3WtHSuI9KRj/Nx/RFvV\n"
                + "l9ufJrE2vHAYyT2kwUvofJmrWileeIHIFk1JfKA2HNB+uz4nfPde5/m9FbWaJoay\n"
                + "40q5c/0CgYEA43HcOblMaX8nzGytaWqqBrd7TUaJbkZlRKiR/Qf4rZxQVdxfKHaO\n"
                + "2UGW7oQdFOa+6HeOaXHQu6G3XalaFoeoQa3+DQGo7uSlpDaoHOD2JBTSJmJi/n/z\n"
                + "wd2mJ/v7xL1NutM/wZquRoryNoJqRpL2h/PLhUJq945VrIR6tZuapfsCgYEArpif\n"
                + "oxaMjj5F41u7FLwy4G7bKKrfGwESJZJKce+FVLnGJ9K2fby3Enkjg1no2L6DtQhz\n"
                + "VkbB4JZQNAsV/Kw7/RZv4fiP7EkzDlkNopKdvtbfBx5JePwcWEgaa8d4ugeuEZZc\n"
                + "fGPN8kbh0qSNDny4kyFa6al171YMyDwiRINGXVMCgYAFfKCwb8ztGpkuMEz6pw0U\n"
                + "/mYmFhaIp4AX6O0kqoWQQp4ZhChzhHCrww5OELfW4j4mw4OW/Zzoed0/kC9RLdKc\n"
                + "SwM+8xRNNwzb6kmh8LdfZXUwYd07gICz45H3kvee5mYypJf8JqwfnYh8UicYLsFt\n"
                + "xC7btx93e2KLt+Jx8SAI8wKBgBPgckQZ3BSRiFlS5yB8MHqWhELD+TRU0eEPH4SL\n"
                + "FS3JHIMKHRaBdoIW4nEguj56qfnhJWhME+RimGzkWjNy1D2C4WfjaVcoGjTm9INu\n"
                + "l6DTS36+9vRcF0oBo2hjxB3BpBKCgLL0lcygPzNA4oIktsqhQH1ben6t2DSvi+Mq\n"
                + "4sbbAoGBAM8vuwPE5W5uI+3nmiu1iryjNFWgZIEbID+Cf0NrFb3CPVPV6wvirwE5\n"
                + "SfxFz1ed2gByEc9Iv7nvqeIB4JYoVkxmKeIktNuTVYiY/In0Py46bW6jlRrjT+t6\n"
                + "lUXc8pEcLGIu1EwFEOknOlaAiQGeNincv0S7h/GxoFXe4/OKHKuz\n"
                + "-----END RSA PRIVATE KEY-----\n";
        String saltSignPublicKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCbHv0em4exfGMKqlwbQswJdrHQMZq2ygxVI9E3u0qGpgbQwFhxSWQ5MFNSeikVxBASOlb"
                + "JwjtCL/SgpIaBED9qiYhf3+ImTeTf4g5FOlXdZbbjSfZ8pe3yq7/3Cf5yDtIsNp6fAspagLkPtBKumObWjycWtwlL0RiGcECvKzftOsXuGLow7qwHVhvzLWrtm5D"
                + "tX1tDO4fyQrT66ZWLE/wWnAAP4RnwujWyZtOp6im9ZentonrO17equaGJ/JlE8zNnojBqAjvqrsRo73pwdGoRVcvtNSsJqJ7jGxIX1fdWN3Y4aLFzoqRAZgRXRgU"
                + "BiLJTUB06WqJXDmnD3k11Ef9h";

        //GatewayConfig gatewayConfig = new GatewayConfig(connectionAddress,publicAddress,privateAddress,gatewayPort, false);
        gatewayConfig = new GatewayConfig(connectionAddress, publicAddress, privateAddress, hostname, gatewayPort, serverCert, clientCert, clientKey,
                saltPassword, saltBootPassword, signatureKey, false, true, saltSignPrivateKey, saltSignPublicKey);
        node = new Node(privateAddress, publicAddress, hostname, HOST_GROUP);
        exitModel = new MyExitCriteriaModel();
    }

    @Test
    void testImage() throws CloudbreakOrchestratorException {
        testIsBootstrapApiAvailable();
        testBootstrap();
        testUploadRecipes();
        testInitServiceRun();
    }

    public void testIsBootstrapApiAvailable() {
        assertTrue(saltOrchestratorUnderTest.isBootstrapApiAvailable(gatewayConfig));
    }

    public void testGetStateConfigZip() throws IOException {
        assertNotNull(saltOrchestratorUnderTest.getStateConfigZip());
    }

    public void testBootstrap() throws CloudbreakOrchestratorException {
        BootstrapParams params = new BootstrapParams();
        params.setCloud(AWS_PLATFORM);
        params.setOs("amazonlinux");
        saltOrchestratorUnderTest.bootstrap(List.of(gatewayConfig), Set.of(node), params, exitModel);
    }

    public void testGetMembers() throws CloudbreakOrchestratorException {
        saltOrchestratorUnderTest.getMissingNodes(gatewayConfig, Set.of(node));
        saltOrchestratorUnderTest.getMembers(gatewayConfig, List.of(privateAddress));
    }

    public void testUploadRecipes() throws CloudbreakOrchestratorFailedException {
        RecipeModel recipe = new RecipeModel("hdfs-home", RecipeType.POST_CLUSTER_INSTALL, SCRIPT);
        Map<String, List<RecipeModel>> recipes = Map.of(HOST_GROUP, List.of(recipe));
        saltOrchestratorUnderTest.uploadRecipes(List.of(gatewayConfig), recipes, exitModel);
    }

    public void testInitServiceRun() throws CloudbreakOrchestratorException {
        SaltPillarProperties ambarigpl =
                new SaltPillarProperties("/ambari/gpl.sls", Map.of("ambari", Map.of("gpl", Map.of("enabled", false))));
        SaltPillarProperties metadata =
                new SaltPillarProperties("/metadata/init.sls", Map.of("cluster", Map.of("name", "testcluster")));
        SaltPillarProperties hdp =
                new SaltPillarProperties("/hdp/repo.sls", Map.of("hdp",
                        Map.of("redhat6", REDHAT_HDP_REPO_URL,
                                "repoid", REPO_ID,
                                "repository-version", HDP_VERSION,
                                "vdf-url", REDHAT_HDP_VDF_URL)));
        SaltPillarProperties discovery =
                new SaltPillarProperties("/discovery/init.sls", Map.of("platform", AWS_PLATFORM));
        SaltPillarProperties postgres =
                new SaltPillarProperties("/postgresql/postgre.sls", Map.of("postgres", Map.of(
                        "hive", Map.of(
                                "database", HIVE_DB,
                                "password", HIVE_DB_PASSWORD,
                                "user", HIVE_USER
                        ),
                        "database", HIVE_DB,
                        "password", HIVE_DB_PASSWORD,
                        "user", HIVE_USER,
                        "ambari", Map.of(
                                "database", AMBARI_DB,
                                "password", AMBARI_DB_PASSWORD,
                                "user", AMBARI_USER
                        )
                )));
        com.sequenceiq.cloudbreak.domain.RDSConfig rdsConfig =
                new com.sequenceiq.cloudbreak.domain.RDSConfig();
        rdsConfig.setConnectionURL("jdbc:postgresql://" + privateAddress + ":5432/" + AMBARI_DB);
        rdsConfig.setDatabaseEngine(DatabaseVendor.POSTGRES);
        rdsConfig.setConnectionPassword(AMBARI_DB_PASSWORD);
        rdsConfig.setConnectionUserName(AMBARI_USER);
        rdsConfig.setConnectionDriver("org.postgresql.Driver");
        rdsConfig.setName(AMBARI_DB);
        rdsConfig.setType(RdsType.AMBARI.name());

        SaltPillarProperties ambariDb =
                new SaltPillarProperties("/ambari/database.sls", Map.of("ambari", Map.of(
                        "database", new RdsView(rdsConfig)
                )));
        SaltPillarProperties ambariCredentials
                = new SaltPillarProperties("/ambari/credentials.sls", Map.of("ambari", Map.of(
                "password", "24evrqe59hfi5doctlf3df949n",
                "securityMasterKey", "bigdata",
                "username", "cloudbreak"
        )));
        AmbariRepo ambariRepo = new AmbariRepo();
        ambariRepo.setVersion(AMBARI_VERSION);
        ambariRepo.setBaseUrl(REDHAT_AMBARI_REPO_URL);
        ambariRepo.setPredefined(true);
        SaltPillarProperties ambariRepository
                = new SaltPillarProperties("/ambari/repo.sls", Map.of("ambari", Map.of("repo", ambariRepo)));
        SaltPillarProperties gateway
                = new SaltPillarProperties("/gateway/init.sls", Map.of("gateway", Map.of(
                "mastersecret", "5tpevchd8m73m7v9mni9borm8k",
                "password", "admin123",
                "address", publicAddress,
                "ssotype", SSOType.NONE,
                "location", Map.of(
                        "HIVE_SERVER", List.of(hostname),
                        "HISTORYSERVER", List.of(hostname),
                        "SPARK_JOBHISTORYSERVER", List.of(hostname),
                        "NAMENODE", List.of(hostname),
                        "ZEPPELIN_MASTER", List.of(hostname),
                        "RESOURCEMANAGER", List.of(hostname)
                ),
                "kerberos", false,
                "username", "admin"
        )));
        SaltPillarProperties docker =
                new SaltPillarProperties("/docker/init.sls", Map.of("docker", Map.of("enableContainerExecutor", false)));
        Map<String, SaltPillarProperties> servicePillarConfig = Map.of(
                "ambari-gpl-repo", ambarigpl,
                "metadata", metadata,
                "hdp", hdp,
                "discovery", discovery,
                "postgresql-server", postgres,
                "ambari-database", ambariDb,
                "ambari-credentials", ambariCredentials,
                "ambari-repo", ambariRepository,
                "gateway", gateway,
                "docker", docker
        );
        Map<String, Map<String, String>> grainsProperites = Map.of(privateAddress, Map.of("gateway-address", publicAddress));
        SaltConfig saltConfig = new SaltConfig(servicePillarConfig, grainsProperites);
        saltOrchestratorUnderTest.initServiceRun(List.of(gatewayConfig), Set.of(node), saltConfig, exitModel);
        saltOrchestratorUnderTest.runService(List.of(gatewayConfig), Set.of(node), saltConfig, exitModel);
    }

    @Configuration
    @ComponentScan("com.sequenceiq.cloudbreak.orchestrator.salt")
    public static class SpringConfig {

        //@Value("${cb.container.threadpool.core.size:}")
        private int containerCorePoolSize = 2;

        //@Value("${cb.container.threadpool.capacity.size:}")
        private int containerteQueueCapacity = 2;

        @Bean
        public SaltOrchestrator saltOrchestrator() {
            SaltOrchestrator saltOrchestrator = new SaltOrchestrator();
            saltOrchestrator.init(simpleParallelContainerRunnerExecutor(), clusterDeletionBasedExitCriteria());
            return saltOrchestrator;
        }

        @Bean
        public ParallelOrchestratorComponentRunner simpleParallelContainerRunnerExecutor() {
            return new TestOrchestratorComponentRunner(containerBootstrapBuilderExecutor());
        }

        @Bean
        public ExitCriteria clusterDeletionBasedExitCriteria() {
            return EXIT_CRITERIA;
        }

        @Bean
        public AsyncTaskExecutor containerBootstrapBuilderExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(containerCorePoolSize);
            executor.setQueueCapacity(containerteQueueCapacity);
            executor.setThreadNamePrefix("containerBootstrapBuilderExecutor-");
            executor.setTaskDecorator(new MDCCleanerTaskDecorator());
            executor.initialize();
            return executor;
        }
    }

    private static class MyExitCriteriaModel extends ExitCriteriaModel {
    }

    static class TestOrchestratorComponentRunner implements ParallelOrchestratorComponentRunner {

        private final AsyncTaskExecutor asyncTaskExecutor;

        TestOrchestratorComponentRunner(AsyncTaskExecutor asyncTaskExecutor) {
            this.asyncTaskExecutor = asyncTaskExecutor;
        }

        @Override
        public Future<Boolean> submit(Callable<Boolean> callable) {
            return asyncTaskExecutor.submit(callable);
        }
    }
}
