/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2011  Imran M Yousuf (imyousuf@smartitengineering.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.smartitengineering.cms.ws.resources.workspace;

import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.workspace.Sequence;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.util.rest.atom.server.AbstractResource;
import com.smartitengineering.util.rest.server.ServerResourceInjectables;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;

/**
 *
 * @author imyousuf
 */
public class WorkspaceSequencesResource extends AbstractResource {

  public static final String PARAM_SEQUENCE_NAME = "sequenceName";
  public static final String PATH_TO_SEQUENCE = "{" + PARAM_SEQUENCE_NAME + "}";
  private final Workspace workspace;

  public WorkspaceSequencesResource(Workspace workspace, ServerResourceInjectables injectables) {
    super(injectables);
    this.workspace = workspace;
  }

  @GET
  @Produces(MediaType.APPLICATION_ATOM_XML)
  public Response get() {
    ResponseBuilder builder = Response.ok();
    Feed feed = getFeed("Sequences");
    Collection<Sequence> sequences = SmartContentAPI.getInstance().getWorkspaceApi().getSequencesForWorkspace(workspace.
        getId());
    for (Sequence sequence : sequences) {
      feed.addEntry(getEntry(sequence.getName(), sequence.getName(), new Date(),
                             getLink(getSequenceUri(getRelativeURIBuilder(), sequence), Link.REL_SELF,
                                     MediaType.APPLICATION_JSON)));
    }
    builder.entity(feed);
    return builder.build();
  }

  @Path(PATH_TO_SEQUENCE)
  public WorkspaceSequenceResource getSequence(@PathParam(PARAM_SEQUENCE_NAME) String seqName) {
    WorkspaceSequenceResource sequenceResource = new WorkspaceSequenceResource(workspace, seqName,
                                                                               getInjectables());
    return sequenceResource;
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response createNewSequence(@FormParam("sequenceName") String sequenceName,
                                    @DefaultValue("0") @FormParam("initialValue") long initialValue) {
    final WorkspaceId id = workspace.getId();
    Sequence sequence = SmartContentAPI.getInstance().getWorkspaceApi().putSequence(id, sequenceName,
                                                                                    initialValue);
    URI uri = getSequenceUri(getAbsoluteURIBuilder(), sequence);
    ResponseBuilder builder = Response.created(uri);
    return builder.build();
  }

  protected URI getSequenceUri(UriBuilder builder, Sequence sequence) throws IllegalArgumentException,
                                                                             UriBuilderException {
    UriBuilder uriBuilder = builder.path(WorkspaceResource.class).path(WorkspaceResource.PATH_SEQUENCES).path(
        PATH_TO_SEQUENCE);
    final URI uri = uriBuilder.build(sequence.getWorkspace().getGlobalNamespace(), sequence.getWorkspace().getName(),
                                     sequence.getName());
    return uri;
  }

  @Override
  protected String getAuthor() {
    return "Smart CMS";
  }
}
