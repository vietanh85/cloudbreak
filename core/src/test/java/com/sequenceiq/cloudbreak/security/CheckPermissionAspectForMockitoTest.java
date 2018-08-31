package com.sequenceiq.cloudbreak.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;

import com.sequenceiq.cloudbreak.aspect.CheckPermissionsAspects;

public class CheckPermissionAspectForMockitoTest extends CheckPermissionsAspects {

    // Mockito generated code (stubbing, as well as call to getMockitoInterceptor activates the AOP joinpoint => false positives)
    private boolean stubbing = true;

    private void turnStubbingOff() {
        stubbing = false;
    }

    private void turnStubbingOn() {
        stubbing = true;
    }

    private boolean isStubbingOn() {
        return stubbing;
    }

    @Override
    @Around("allRepositories()")
    public Object hasPermission(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        if (!isStubbingOn() && !proceedingJoinPoint.getSignature().getName().contains("Mockito")) {
            return super.hasPermission(proceedingJoinPoint);
        }
        return proceedingJoinPoint.proceed();
    }

    public class StubbingDeactivator implements AutoCloseable {

        public StubbingDeactivator() {
            turnStubbingOff();
        }

        @Override
        public void close() throws Exception {
            turnStubbingOn();
        }
    }
}
