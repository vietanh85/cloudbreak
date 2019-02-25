package com.sequenceiq.datalake.api.endpoint.v1;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sequenceiq.datalake.api.model.RegisterDatalakeRequest;
import com.sequenceiq.datalake.api.model.RegisterDatalakeResponse;
import com.sequenceiq.datalake.doc.ApiDescription;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/v1/datalake")
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "/v1/datalake", description = ApiDescription.REGISTER_DATALAKE_DESCRIPTION, protocols = "http,https")
public interface DatalakeEndpoint {

    @POST
    @Path("register")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = ApiDescription.DatalakeOpDescription.REGISTER_POST,
            produces = MediaType.APPLICATION_JSON,
            notes = ApiDescription.DatalakeNotes.REGISTER_NOTES)
    RegisterDatalakeResponse registerDatalake(@Valid RegisterDatalakeRequest request);

}
