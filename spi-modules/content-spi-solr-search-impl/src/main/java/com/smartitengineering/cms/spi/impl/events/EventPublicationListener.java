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
package com.smartitengineering.cms.spi.impl.events;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.event.Event;
import com.smartitengineering.cms.api.event.Event.Type;
import com.smartitengineering.cms.api.event.EventListener;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.workspace.Sequence;
import com.smartitengineering.cms.api.workspace.SequenceId;
import com.smartitengineering.events.async.api.EventPublisher;
import java.io.ByteArrayOutputStream;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
@Singleton
public class EventPublicationListener implements EventListener {

  @Inject
  private EventPublisher publisher;
  private final transient Logger logger = LoggerFactory.getLogger(getClass());
  static final byte[] NEW_LINE = org.apache.commons.codec.binary.StringUtils.getBytesUtf8("\n");

  @Override
  public boolean accepts(Event event) {
    final Type eventSourceType = event.getEventSourceType();
    return eventSourceType.equals(Type.CONTENT) || eventSourceType.equals(Type.CONTENT_TYPE) ||
        eventSourceType.equals(Type.SEQUENCE);
  }

  @Override
  public void notify(Event event) {
    final String hexedContentId;
    final Type eventSourceType = event.getEventSourceType();
    try {
      ByteArrayOutputStream contentBytes = new ByteArrayOutputStream();
      if (eventSourceType.equals(Type.CONTENT)) {
        ContentId contentId = ((Content) event.getSource()).getContentId();
        contentBytes.write(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(contentId.getWorkspaceId().
            getGlobalNamespace()));
        contentBytes.write(NEW_LINE);
        contentBytes.write(
            org.apache.commons.codec.binary.StringUtils.getBytesUtf8(contentId.getWorkspaceId().getName()));
        contentBytes.write(NEW_LINE);
        contentBytes.write(contentId.getId());
      }
      else if (eventSourceType.equals(Type.CONTENT_TYPE)) {
        ContentTypeId typeId = ((ContentType) event.getSource()).getContentTypeID();
        contentBytes.write(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(typeId.getWorkspace().
            getGlobalNamespace()));
        contentBytes.write(NEW_LINE);
        contentBytes.write(
            org.apache.commons.codec.binary.StringUtils.getBytesUtf8(typeId.getWorkspace().getName()));
        contentBytes.write(NEW_LINE);
        contentBytes.write(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(typeId.getNamespace()));
        contentBytes.write(NEW_LINE);
        contentBytes.write(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(typeId.getName()));
      }
      else if (eventSourceType.equals(Type.SEQUENCE)) {
        SequenceId seqId = ((Sequence) event.getSource()).getSequenceId();
        contentBytes.write(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(seqId.getWorkspaceId().
            getGlobalNamespace()));
        contentBytes.write(NEW_LINE);
        contentBytes.write(
            org.apache.commons.codec.binary.StringUtils.getBytesUtf8(seqId.getWorkspaceId().getName()));
        contentBytes.write(NEW_LINE);
        contentBytes.write(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(seqId.getName()));
      }
      else {
        logger.warn("Unrecognized event source type!");
      }
      hexedContentId = Base64.encodeBase64URLSafeString(contentBytes.toByteArray());
      if (logger.isDebugEnabled()) {
        logger.debug("Content of event message " + hexedContentId);
      }
    }
    catch (Exception ex) {
      logger.warn("Could not serialize content ID!", ex);
      return;
    }
    if (StringUtils.isNotBlank(hexedContentId)) {
      String message = new StringBuilder(eventSourceType.name()).append('\n').append(event.getEventType().
          name()).append('\n').append(hexedContentId).toString();
      if (logger.isDebugEnabled()) {
        logger.debug("Publishing message " + message);
      }
      publisher.publishEvent("text/plain", message);
    }
  }
}
