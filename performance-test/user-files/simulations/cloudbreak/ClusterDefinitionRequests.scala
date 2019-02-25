package cloudbreak

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

object ClusterDefinitionRequests {

    val createClusterDefinition = http("create cluster definition")
      .post("/cb/api/v4/${workspaceId}/cluster_definitions/user")
      .headers(HttpHeaders.commonHeaders)
      .body(ElFileBody("./simulations/cloudbreak/resources/create-cluster-definition-base64.json"))
      .check(status.is(200), jsonPath("$.id").saveAs("clusterDefinitionId"))

    val deleteClusterDefinition = http("delete cluster definition")
      .delete("/cb/api/v4/${workspaceId}/cluster_definitions/${clusterDefinitionId}")
      .headers(HttpHeaders.commonHeaders)
      .check(status.is(204))
}