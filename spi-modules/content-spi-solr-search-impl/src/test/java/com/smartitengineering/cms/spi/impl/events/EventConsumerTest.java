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
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.event.Event;
import com.smartitengineering.cms.api.event.Event.EventType;
import com.smartitengineering.cms.api.event.Event.Type;
import com.smartitengineering.cms.api.event.EventListener;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.content.ContentLoader;
import com.smartitengineering.cms.api.factory.event.EventRegistrar;
import com.smartitengineering.cms.api.factory.type.ContentTypeLoader;
import com.smartitengineering.cms.api.factory.workspace.WorkspaceAPI;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.workspace.Sequence;
import com.smartitengineering.cms.api.workspace.SequenceId;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.lock.LockHandler;
import com.smartitengineering.dao.solr.SolrWriteDao;
import com.smartitengineering.events.async.api.EventConsumer;
import com.smartitengineering.util.bean.BeanFactoryRegistrar;
import com.smartitengineering.util.bean.guice.GoogleGuiceBeanFactory;
import java.lang.reflect.Field;
import java.util.Arrays;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author imyousuf
 */
public class EventConsumerTest {

  private final static String MSG_TYPE = "text/plain";
  private Mockery mockery;
  private EventListener<Content> contentListener;
  private EventListener<ContentType> contentTypeListener;
  private EventListener<Sequence> sequenceListener;
  private SolrWriteDao solrWriteDao;
  private ContentTypeLoader contentTypeLoader;
  private ContentLoader contentLoader;
  private WorkspaceAPI workspaceApi;
  private EventRegistrar eventRegistrar;
  private LockHandler lockHandler;
  private Injector injector;

  @Before
  public void setup() throws Exception {
    mockery = new Mockery();
    contentListener = mockery.mock(EventListener.class, "contentEventListener");
    contentTypeListener = mockery.mock(EventListener.class,
                                       "contentTypeEventListener");
    sequenceListener = mockery.mock(EventListener.class, "sequenceEventListener");
    solrWriteDao = mockery.mock(SolrWriteDao.class);
    contentTypeLoader = mockery.mock(ContentTypeLoader.class);
    contentLoader = mockery.mock(ContentLoader.class);
    workspaceApi = mockery.mock(WorkspaceAPI.class);
    eventRegistrar = mockery.mock(EventRegistrar.class);
    lockHandler = mockery.mock(LockHandler.class);
    injector = Guice.createInjector(new EventConsumptionImplModule());
    Field field = SmartContentAPI.class.getDeclaredField("api");
    field.setAccessible(true);
    field.set(null, null);
    field = SmartContentSPI.class.getDeclaredField("spi");
    field.setAccessible(true);
    field.set(null, null);
    final GoogleGuiceBeanFactory googleGuiceBeanFactory = new GoogleGuiceBeanFactory(true, injector);
    BeanFactoryRegistrar.registerBeanFactory(SmartContentAPI.CONTEXT_NAME, googleGuiceBeanFactory);
    BeanFactoryRegistrar.registerBeanFactory(SmartContentSPI.SPI_CONTEXT, googleGuiceBeanFactory);
  }

  @Test(expected = RuntimeException.class)
  public void testInvalidType() {
    mockery.checking(new Expectations() {

      {
      }
    });
    EventConsumer consumer = injector.getInstance(EventConsumer.class);
    consumer.consume(MSG_TYPE, "MY\nCREATE\n" + Base64.encodeBase64URLSafeString(StringUtils.getBytesUtf8(
        "random string\nfor test")));
    mockery.assertIsSatisfied();
  }

  @Test(expected = RuntimeException.class)
  public void testInvalidEventType() {
    mockery.checking(new Expectations() {

      {
      }
    });
    EventConsumer consumer = injector.getInstance(EventConsumer.class);
    consumer.consume(MSG_TYPE, "CONTENT\nCREATER\n" + Base64.encodeBase64URLSafeString(StringUtils.getBytesUtf8(
        "random string\nfor test")));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testContentConsumption() {
    mockery.checking(new Expectations() {

      {
        exactly(1).of(workspaceApi).createWorkspaceId(EventPublisherTest.WORSPACE_NS, EventPublisherTest.WORKSPACE_NAME);
        final WorkspaceId wId = mockery.mock(WorkspaceId.class);
        will(returnValue(wId));
        exactly(1).of(contentLoader).createContentId(with(wId), with(new BaseMatcher<byte[]>() {

          public boolean matches(Object item) {
            return Arrays.equals(StringUtils.getBytesUtf8(EventPublisherTest.CONTENT_ID), (byte[]) item);
          }

          public void describeTo(Description description) {
          }
        }));
        final ContentId contentId = mockery.mock(ContentId.class);
        will(returnValue(contentId));
        exactly(1).of(contentId).getContent();
        final Content content = mockery.mock(Content.class);
        will(returnValue(content));
        exactly(1).of(eventRegistrar).createEvent(EventType.CREATE, Type.CONTENT, content);
        final Event<Content> event = mockery.mock(Event.class);
        will(returnValue(event));
        exactly(1).of(contentListener).notify(event);
      }
    });
    EventConsumer consumer = injector.getInstance(EventConsumer.class);
    consumer.consume(MSG_TYPE, EventPublisherTest.getContentMsg());
    mockery.assertIsSatisfied();
  }

  @Test
  public void testContentConsumptionWithInvalidMessage() {
    mockery.checking(new Expectations() {

      {
      }
    });
    EventConsumer consumer = injector.getInstance(EventConsumer.class);
    consumer.consume(MSG_TYPE, "CONTENT\nCREATE\n" + Base64.encodeBase64URLSafeString(StringUtils.getBytesUtf8(
        "random string\nfor test")));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testContentIgnorance() {
    mockery.checking(new Expectations() {

      {
        exactly(1).of(workspaceApi).createWorkspaceId(EventPublisherTest.WORSPACE_NS, EventPublisherTest.WORKSPACE_NAME);
        final WorkspaceId wId = mockery.mock(WorkspaceId.class);
        will(returnValue(wId));
        exactly(1).of(contentLoader).createContentId(with(wId), with(new BaseMatcher<byte[]>() {

          public boolean matches(Object item) {
            return Arrays.equals(StringUtils.getBytesUtf8(EventPublisherTest.CONTENT_ID), (byte[]) item);
          }

          public void describeTo(Description description) {
          }
        }));
        final ContentId contentId = mockery.mock(ContentId.class);
        will(returnValue(contentId));
        exactly(1).of(contentId).getContent();
        will(returnValue(null));
      }
    });
    EventConsumer consumer = injector.getInstance(EventConsumer.class);
    consumer.consume(MSG_TYPE, EventPublisherTest.getContentMsg());
    mockery.assertIsSatisfied();
  }

  @Test
  public void testContentTypeConsumption() {
    mockery.checking(new Expectations() {

      {
        exactly(1).of(workspaceApi).createWorkspaceId(EventPublisherTest.WORSPACE_NS, EventPublisherTest.WORKSPACE_NAME);
        final WorkspaceId wId = mockery.mock(WorkspaceId.class);
        will(returnValue(wId));
        exactly(1).of(contentTypeLoader).createContentTypeId(wId, EventPublisherTest.CONTENT_TYPE_NS,
                                                             EventPublisherTest.CONTENT_TYPE_NAME);
        final ContentTypeId typeId = mockery.mock(ContentTypeId.class);
        will(returnValue(typeId));
        exactly(1).of(typeId).getContentType();
        final ContentType type = mockery.mock(ContentType.class);
        will(returnValue(type));
        exactly(1).of(eventRegistrar).createEvent(EventType.CREATE, Type.CONTENT_TYPE, type);
        final Event<ContentType> event = mockery.mock(Event.class);
        will(returnValue(event));
        exactly(1).of(contentTypeListener).notify(event);
      }
    });
    EventConsumer consumer = injector.getInstance(EventConsumer.class);
    consumer.consume(MSG_TYPE, EventPublisherTest.getContentTypeMsg());
    mockery.assertIsSatisfied();
  }

  @Test
  public void testContentTypeConsumptionWithInvalidMessage() {
    mockery.checking(new Expectations() {

      {
      }
    });
    EventConsumer consumer = injector.getInstance(EventConsumer.class);
    consumer.consume(MSG_TYPE, "CONTENT_TYPE\nCREATE\n" + Base64.encodeBase64URLSafeString(StringUtils.getBytesUtf8(
        "random string\nfor test\nof content type")));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testContentTypeIgnorance() {
    mockery.checking(new Expectations() {

      {
        exactly(1).of(workspaceApi).createWorkspaceId(EventPublisherTest.WORSPACE_NS, EventPublisherTest.WORKSPACE_NAME);
        final WorkspaceId wId = mockery.mock(WorkspaceId.class);
        will(returnValue(wId));
        exactly(1).of(contentTypeLoader).createContentTypeId(wId, EventPublisherTest.CONTENT_TYPE_NS,
                                                             EventPublisherTest.CONTENT_TYPE_NAME);
        final ContentTypeId typeId = mockery.mock(ContentTypeId.class);
        will(returnValue(typeId));
        exactly(1).of(typeId).getContentType();
        will(returnValue(null));
      }
    });
    EventConsumer consumer = injector.getInstance(EventConsumer.class);
    consumer.consume(MSG_TYPE, EventPublisherTest.getContentTypeMsg());
    mockery.assertIsSatisfied();
  }

  @Test
  public void testSequenceConsumption() {
    mockery.checking(new Expectations() {

      {
        exactly(1).of(workspaceApi).createWorkspaceId(EventPublisherTest.WORSPACE_NS, EventPublisherTest.WORKSPACE_NAME);
        final WorkspaceId wId = mockery.mock(WorkspaceId.class);
        will(returnValue(wId));
        exactly(1).of(workspaceApi).createSequenceId(wId, EventPublisherTest.SEQUENCE_NAME);
        final SequenceId seqId = mockery.mock(SequenceId.class);
        will(returnValue(seqId));
        exactly(1).of(seqId).getSequence();
        final Sequence sequence = mockery.mock(Sequence.class);
        will(returnValue(sequence));
        exactly(1).of(eventRegistrar).createEvent(EventType.CREATE, Type.SEQUENCE, sequence);
        final Event<Sequence> event = mockery.mock(Event.class);
        will(returnValue(event));
        exactly(1).of(sequenceListener).notify(event);
      }
    });
    EventConsumer consumer = injector.getInstance(EventConsumer.class);
    consumer.consume(MSG_TYPE, EventPublisherTest.getSequenceMsg());
    mockery.assertIsSatisfied();
  }

  @Test
  public void testSequenceConsumptionWithInvalidMessage() {
    mockery.checking(new Expectations() {

      {
      }
    });
    EventConsumer consumer = injector.getInstance(EventConsumer.class);
    consumer.consume(MSG_TYPE, "SEQUENCE\nCREATE\n" + Base64.encodeBase64URLSafeString(StringUtils.getBytesUtf8(
        "random string\nfor test")));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testSequenceIgnorance() {
    mockery.checking(new Expectations() {

      {
        exactly(1).of(workspaceApi).createWorkspaceId(EventPublisherTest.WORSPACE_NS, EventPublisherTest.WORKSPACE_NAME);
        final WorkspaceId wId = mockery.mock(WorkspaceId.class);
        will(returnValue(wId));
        exactly(1).of(workspaceApi).createSequenceId(wId, EventPublisherTest.SEQUENCE_NAME);
        final SequenceId seqId = mockery.mock(SequenceId.class);
        will(returnValue(seqId));
        exactly(1).of(seqId).getSequence();
        will(returnValue(null));
      }
    });
    EventConsumer consumer = injector.getInstance(EventConsumer.class);
    consumer.consume(MSG_TYPE, EventPublisherTest.getSequenceMsg());
    mockery.assertIsSatisfied();
  }

  private final class EventConsumptionImplModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(SolrWriteDao.class).toInstance(solrWriteDao);
      bind(new TypeLiteral<EventListener<Content>>() {
      }).toInstance(contentListener);
      bind(new TypeLiteral<EventListener<ContentType>>() {
      }).toInstance(contentTypeListener);
      bind(new TypeLiteral<EventListener<Sequence>>() {
      }).toInstance(sequenceListener);
      bind(EventConsumer.class).to(EventConsumerImpl.class);
      // For SmartContentAPI
      bind(ContentTypeLoader.class).annotatedWith(Names.named("apiContentTypeLoader")).toInstance(contentTypeLoader);
      bind(ContentLoader.class).annotatedWith(Names.named("apiContentLoader")).toInstance(contentLoader);
      bind(WorkspaceAPI.class).annotatedWith(Names.named("apiWorkspaceApi")).toInstance(workspaceApi);
      bind(EventRegistrar.class).annotatedWith(Names.named("apiEventRegistrar")).toInstance(eventRegistrar);
      bind(LockHandler.class).toInstance(lockHandler);
    }
  }
}
