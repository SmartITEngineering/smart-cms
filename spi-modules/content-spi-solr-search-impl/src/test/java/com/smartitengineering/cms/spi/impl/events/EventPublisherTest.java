/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2012  Imran M Yousuf (imyousuf@smartitengineering.com)
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
package com.smartitengineering.cms.spi.impl.events;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.event.Event;
import com.smartitengineering.cms.api.event.EventListener;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.workspace.Sequence;
import com.smartitengineering.cms.api.workspace.SequenceId;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.events.async.api.EventPublisher;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author imyousuf
 */
public class EventPublisherTest {

  public static final String CONTENT_ID = "someId";
  public static final String WORKSPACE_NAME = "globalWorkspaceName";
  public static final String WORSPACE_NS = "globalWorkspaceNamespace";
  public static final String CONTENT_TYPE_NAME = "contentTypeName";
  public static final String CONTENT_TYPE_NS = "contentTypeNamespace";
  public static final String SEQUENCE_NAME = "globalWorkspaceName";
  private final Mockery mockery = new Mockery();
  private final EventPublisher mockPublisher = mockery.mock(EventPublisher.class);
  private Injector injector;

  @Before
  public void setup() {
    injector = Guice.createInjector(new EventPublicationListenerModule());
  }

  @Test
  public void testPublicationOfContent() {
    EventListener listener = injector.getInstance(EventListener.class);
    final Event mockEvent = mockery.mock(Event.class);
    final Content mockContent = mockery.mock(Content.class);
    final ContentId mockContentId = mockery.mock(ContentId.class);
    final WorkspaceId mockWorkspaceId = mockery.mock(WorkspaceId.class);
    final String msgContent = getContentMsg();
    mockery.checking(new Expectations() {

      {
        exactly(1).of(mockEvent).getEventSourceType();
        will(returnValue(Event.Type.CONTENT));
        exactly(1).of(mockEvent).getEventType();
        will(returnValue(Event.EventType.CREATE));
        exactly(1).of(mockEvent).getSource();
        will(returnValue(mockContent));
        exactly(1).of(mockContent).getContentId();
        will(returnValue(mockContentId));
        exactly(2).of(mockContentId).getWorkspaceId();
        will(returnValue(mockWorkspaceId));
        exactly(1).of(mockContentId).getId();
        will(returnValue(StringUtils.getBytesUtf8(CONTENT_ID)));
        exactly(1).of(mockWorkspaceId).getGlobalNamespace();
        will(returnValue(WORSPACE_NS));
        exactly(1).of(mockWorkspaceId).getName();
        will(returnValue(WORKSPACE_NAME));
        exactly(1).of(mockPublisher).publishEvent(with("text/plain"), with(msgContent));
        will(returnValue(Boolean.TRUE));
      }
    });
    listener.notify(mockEvent);
    mockery.assertIsSatisfied();
  }

  @Test
  public void testPublicationOfContentType() {
    EventListener listener = injector.getInstance(EventListener.class);
    final Event mockEvent = mockery.mock(Event.class);
    final ContentType mockContentType = mockery.mock(ContentType.class);
    final ContentTypeId mockContentTypeId = mockery.mock(ContentTypeId.class);
    final WorkspaceId mockWorkspaceId = mockery.mock(WorkspaceId.class);
    final String msgContent = getContentTypeMsg();
    mockery.checking(new Expectations() {

      {
        exactly(1).of(mockEvent).getEventSourceType();
        will(returnValue(Event.Type.CONTENT_TYPE));
        exactly(1).of(mockEvent).getEventType();
        will(returnValue(Event.EventType.CREATE));
        exactly(1).of(mockEvent).getSource();
        will(returnValue(mockContentType));
        exactly(1).of(mockContentType).getContentTypeID();
        will(returnValue(mockContentTypeId));
        exactly(2).of(mockContentTypeId).getWorkspace();
        will(returnValue(mockWorkspaceId));
        exactly(1).of(mockContentTypeId).getNamespace();
        will(returnValue(CONTENT_TYPE_NS));
        exactly(1).of(mockContentTypeId).getName();
        will(returnValue(CONTENT_TYPE_NAME));
        exactly(1).of(mockWorkspaceId).getGlobalNamespace();
        will(returnValue(WORSPACE_NS));
        exactly(1).of(mockWorkspaceId).getName();
        will(returnValue(WORKSPACE_NAME));
        exactly(1).of(mockPublisher).publishEvent(with("text/plain"), with(msgContent));
        will(returnValue(Boolean.TRUE));
      }
    });
    listener.notify(mockEvent);
    mockery.assertIsSatisfied();
  }

  @Test
  public void testPublicationOfSequence() {
    EventListener listener = injector.getInstance(EventListener.class);
    final Event mockEvent = mockery.mock(Event.class);
    final Sequence mockSequence = mockery.mock(Sequence.class);
    final SequenceId mockSequenceId = mockery.mock(SequenceId.class);
    final WorkspaceId mockWorkspaceId = mockery.mock(WorkspaceId.class);
    final String msgContent = getSequenceMsg();
    mockery.checking(new Expectations() {

      {
        exactly(1).of(mockEvent).getEventSourceType();
        will(returnValue(Event.Type.SEQUENCE));
        exactly(1).of(mockEvent).getEventType();
        will(returnValue(Event.EventType.CREATE));
        exactly(1).of(mockEvent).getSource();
        will(returnValue(mockSequence));
        exactly(1).of(mockSequence).getSequenceId();
        will(returnValue(mockSequenceId));
        exactly(2).of(mockSequenceId).getWorkspaceId();
        will(returnValue(mockWorkspaceId));
        exactly(1).of(mockSequenceId).getName();
        will(returnValue(SEQUENCE_NAME));
        exactly(1).of(mockWorkspaceId).getGlobalNamespace();
        will(returnValue(WORSPACE_NS));
        exactly(1).of(mockWorkspaceId).getName();
        will(returnValue(WORKSPACE_NAME));
        exactly(1).of(mockPublisher).publishEvent(with("text/plain"), with(msgContent));
        will(returnValue(Boolean.TRUE));
      }
    });
    listener.notify(mockEvent);
    mockery.assertIsSatisfied();
  }

  @Test
  public void testPublicationOfUnknown() {
    EventListener listener = injector.getInstance(EventListener.class);
    final Event mockEvent = mockery.mock(Event.class);
    mockery.checking(new Expectations() {

      {
        exactly(1).of(mockEvent).getEventSourceType();
        will(returnValue(Event.Type.REPRESENTATION));
      }
    });
    listener.notify(mockEvent);
    mockery.assertIsSatisfied();
  }

  private final class EventPublicationListenerModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(EventPublisher.class).toInstance(mockPublisher);
      bind(EventListener.class).to(EventPublicationListener.class);
    }
  }

  public static String getContentMsg() {
    final byte[] contentId = StringUtils.getBytesUtf8(new StringBuilder(WORSPACE_NS).append('\n').append(WORKSPACE_NAME).
        append('\n').append(CONTENT_ID).toString());
    final String msgContent = new StringBuilder("CONTENT\nCREATE\n").append(Base64.encodeBase64URLSafeString(contentId)).
        toString();
    return msgContent;
  }

  public static String getContentTypeMsg() {
    final byte[] contentId = StringUtils.getBytesUtf8(new StringBuilder(WORSPACE_NS).append('\n').append(WORKSPACE_NAME).
        append('\n').append(CONTENT_TYPE_NS).append('\n').append(CONTENT_TYPE_NAME).toString());
    final String msgContent = new StringBuilder("CONTENT_TYPE\nCREATE\n").append(Base64.encodeBase64URLSafeString(
        contentId)).
        toString();
    return msgContent;
  }

  public static String getSequenceMsg() {
    final byte[] contentId = StringUtils.getBytesUtf8(new StringBuilder(WORSPACE_NS).append('\n').append(WORKSPACE_NAME).
        append('\n').append(SEQUENCE_NAME).toString());
    final String msgContent =
                 new StringBuilder("SEQUENCE\nCREATE\n").append(Base64.encodeBase64URLSafeString(contentId)).
        toString();
    return msgContent;
  }
}
