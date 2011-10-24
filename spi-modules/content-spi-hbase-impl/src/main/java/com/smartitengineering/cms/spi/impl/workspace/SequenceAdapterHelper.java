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
package com.smartitengineering.cms.spi.impl.workspace;

import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.workspace.Sequence;
import com.smartitengineering.cms.api.workspace.SequenceId;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.workspace.PersistableSequence;
import com.smartitengineering.util.bean.adapter.AbstractAdapterHelper;

/**
 *
 * @author imyousuf
 */
public class SequenceAdapterHelper extends AbstractAdapterHelper<Sequence, PersistentSequence> {

  @Override
  protected PersistentSequence newTInstance() {
    return new PersistentSequence();
  }

  @Override
  protected void mergeFromF2T(Sequence fromBean, PersistentSequence toBean) {
    SequenceId id = SmartContentAPI.getInstance().getWorkspaceApi().createSequenceId(fromBean.getWorkspace(), fromBean.
        getName());
    toBean.setId(id);
    toBean.setCurrentValue(fromBean.getCurrentValue());
  }

  @Override
  protected Sequence convertFromT2F(PersistentSequence toBean) {
    PersistableSequence sequence =
                        SmartContentSPI.getInstance().getPersistableDomainFactory().createPersistableSequence();
    sequence.setCurrentValue(toBean.getCurrentValue());
    sequence.setName(toBean.getId().getName());
    sequence.setWorkspace(toBean.getId().getWorkspaceId());
    return sequence;
  }
}
