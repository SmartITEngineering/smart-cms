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
package com.smartitengineering.cms.content.api.impl;

import com.smartitengineering.cms.api.common.CacheableResource;
import com.smartitengineering.cms.api.content.template.RepresentationGenerator;
import com.smartitengineering.cms.api.impl.workspace.WorkspaceResourceCacheKey;
import com.smartitengineering.cms.api.impl.workspace.WorkspaceAPIImpl;
import com.smartitengineering.cms.api.workspace.RepresentationTemplate;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.dao.common.cache.CacheServiceProvider;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;

/**
 *
 * @author imyousuf
 */
public class WorkspaceResourceReadThroughCacheTest extends TestCase {

  private Mockery mockery;
  private WorkspaceAPIImpl api;
  private CacheServiceProvider<WorkspaceResourceCacheKey, CacheableResource> mockProvider;
  private RepresentationTemplate mockRepresentationTemplate;
  private RepresentationGenerator mockRepresentationGenerator;
  private WorkspaceId mockId;
  private static final String NAME = "name";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    mockery = new JUnit3Mockery();
    mockProvider = mockery.mock(CacheServiceProvider.class);
    mockRepresentationTemplate = mockery.mock(RepresentationTemplate.class);
    mockRepresentationGenerator = mockery.mock(RepresentationGenerator.class);
    mockId = mockery.mock(WorkspaceId.class);
    api = new WorkspaceAPIImpl() {

      @Override
      public RepresentationGenerator getRepresentationGenerator(RepresentationTemplate representationTemplate) {
        return mockRepresentationGenerator;
      }

      @Override
      public RepresentationTemplate getRepresentationTemplate(WorkspaceId workspaceId, String name,
                                                              boolean searchInFriendlies) {
        return mockRepresentationTemplate;
      }
    };
    api.setResourcesCache(mockProvider);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testWhenNoCache() {
    api.setResourcesCache(null);
    mockery.checking(new Expectations() {

      {
        never(mockProvider);
        never(mockRepresentationGenerator);
        never(mockRepresentationTemplate);
        never(mockId);
      }
    });
    Assert.assertSame(mockRepresentationGenerator, api.getRepresentationGenerator(mockId, NAME,
                                                                                  true));
  }

  public void testWhenNotInCache() {
    mockery.checking(new Expectations() {

      {
        never(mockRepresentationGenerator);
        never(mockRepresentationTemplate);
        never(mockId);
        final WorkspaceResourceCacheKey key =
                                        new WorkspaceResourceCacheKey(mockId,
                                                                      WorkspaceResourceCacheKey.WorkspaceResourceType.REPRESENTATION_GEN,
                                                                      NAME);
        exactly(1).of(mockProvider).retrieveFromCache(with(equal(key)));
        will(returnValue(null));
        exactly(1).of(mockProvider).putToCache(with(equal(key)), with(same(mockRepresentationGenerator)));
      }
    });
    Assert.assertSame(mockRepresentationGenerator, api.getRepresentationGenerator(mockId, NAME,
                                                                                  true));
  }

  public void testWhenInCache() {
    mockery.checking(new Expectations() {

      {
        never(mockRepresentationGenerator);
        never(mockRepresentationTemplate);
        never(mockId);
        final WorkspaceResourceCacheKey key =
                                        new WorkspaceResourceCacheKey(mockId,
                                                                      WorkspaceResourceCacheKey.WorkspaceResourceType.REPRESENTATION_GEN,
                                                                      NAME);
        exactly(1).of(mockProvider).retrieveFromCache(with(equal(key)));
        will(returnValue(mockRepresentationGenerator));
      }
    });
    Assert.assertSame(mockRepresentationGenerator, api.getRepresentationGenerator(mockId, NAME,
                                                                                  true));
  }
}
