package com.smartitengineering.cms.repo.dao.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.smartitengineering.cms.api.common.SearchResult;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.Filter;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.content.WriteableContent;
import com.smartitengineering.cms.api.type.CollectionDataType;
import com.smartitengineering.cms.api.type.CompositeDataType;
import com.smartitengineering.cms.api.type.ContentDataType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.DataType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.FieldValueType;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.dao.common.CommonDao;
import com.smartitengineering.dao.common.queryparam.BasicCompoundQueryParameter;
import com.smartitengineering.dao.common.queryparam.BiOperandQueryParameter;
import com.smartitengineering.dao.common.queryparam.CompositionQueryParameter;
import com.smartitengineering.dao.common.queryparam.MatchMode;
import com.smartitengineering.dao.common.queryparam.OperatorType;
import com.smartitengineering.dao.common.queryparam.Order;
import com.smartitengineering.dao.common.queryparam.QueryParameter;
import com.smartitengineering.dao.common.queryparam.QueryParameterCastHelper;
import com.smartitengineering.dao.common.queryparam.QueryParameterFactory;
import com.smartitengineering.dao.common.queryparam.QueryParameterWithPropertyName;
import com.smartitengineering.dao.common.queryparam.SimpleNameValueQueryParameter;
import com.smartitengineering.dao.common.queryparam.StringLikeQueryParameter;
import com.smartitengineering.dao.common.queryparam.UniOperandQueryParameter;
import com.smartitengineering.dao.common.queryparam.ValueOnlyQueryParameter;
import com.smartitengineering.domain.PersistentDTO;
import com.smartitengineering.util.bean.adapter.GenericAdapter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
@Singleton
public class RepositoryDaoImpl<T extends AbstractRepositoryDomain<? extends PersistentDTO>>
    implements CommonDao<T, String>, ExtendedReadDao<T, String> {

  @Inject
  private WorkspaceId defaultContainerWorkspace;
  @Inject
  private GenericAdapter<Content, T> adapter;
  protected final Class<T> beanClass;
  private ContentTypeId contentTypeId;
  protected transient final Logger logger = LoggerFactory.getLogger(getClass());
  private static final String SOLR_DATE_FORMAT = DateFormatUtils.ISO_DATETIME_FORMAT.getPattern() + "'Z'";

  @Inject
  public RepositoryDaoImpl(Class<T> entityClass) {
    beanClass = entityClass;
  }

  protected ContentTypeId getContentTypeId() {
    if (contentTypeId == null) {
      String typeNS = beanClass.getPackage().getName();
      String typeName = beanClass.getSimpleName();
      contentTypeId = SmartContentAPI.getInstance().getContentTypeLoader().createContentTypeId(defaultContainerWorkspace,
                                                                                               typeNS, typeName);
    }
    return contentTypeId;
  }

  protected Filter getDefaultFilter() {
    Filter filter = SmartContentAPI.getInstance().getContentLoader().craeteFilter();
    filter.addContentTypeToFilter(getContentTypeId());
    filter.setWorkspaceId(defaultContainerWorkspace);
    filter.setFriendliesIncluded(true);
    return filter;
  }

  protected final Class<? extends T> initializeEntityClassFromGenerics() {
    Class<? extends T> extractedEntityClass = null;
    try {
      Type paramType =
           ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
      if (paramType instanceof ParameterizedType) {
        paramType = ((ParameterizedType) paramType).getRawType();
      }
      Class<T> pesistenceRegistryClass = paramType instanceof Class ? (Class<T>) paramType : null;
      if (logger.isDebugEnabled()) {
        logger.debug("Entity class predicted to: " + pesistenceRegistryClass.toString());
      }
      extractedEntityClass = pesistenceRegistryClass;
    }
    catch (Exception ex) {
      logger.warn("Could not predict entity class ", ex);
    }
    return extractedEntityClass;
  }

  protected ContentId getContentId(String id) {
    final ContentId cId;
    if (id.contains(":")) {
      String[] idTokens = id.split(":");
      if (idTokens.length != 3) {
        final byte[] bytesUtf8 = org.apache.commons.codec.binary.StringUtils.getBytesUtf8(idTokens[0]);
        cId = SmartContentAPI.getInstance().getContentLoader().createContentId(defaultContainerWorkspace, bytesUtf8);
      }
      else {
        final byte[] bytesUtf8 = org.apache.commons.codec.binary.StringUtils.getBytesUtf8(idTokens[2]);
        cId = SmartContentAPI.getInstance().getContentLoader().createContentId(SmartContentAPI.getInstance().
            getWorkspaceApi().createWorkspaceId(idTokens[0], idTokens[1]), bytesUtf8);
      }
    }
    else {
      final byte[] bytesUtf8 = org.apache.commons.codec.binary.StringUtils.getBytesUtf8(id);
      cId = SmartContentAPI.getInstance().getContentLoader().createContentId(defaultContainerWorkspace, bytesUtf8);
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Attempting to read content with passed ID " + id + " and reading " + cId);
    }

    return cId;
  }

  public Set<T> getByIds(List<String> ids) {
    Set<T> cs = new LinkedHashSet<T>();
    for (String id : ids) {
      T bean = getById(id);
      if (bean != null) {
        cs.add(bean);
      }
    }
    return cs;
  }

  public T getById(String id) {
    ContentId contentId = getContentId(id);
    Content content = SmartContentAPI.getInstance().getContentLoader().loadContent(contentId);
    if (content == null) {
      return null;
    }
    else {
      return adapter.convert(content);
    }
  }

  public Set<T> getAll() {
    return new LinkedHashSet<T>(searchCms(getDefaultFilter(), true));
  }

  public T getSingle(List<QueryParameter> queries) {
    Filter filter = getDefaultFilter();
    processQueryParams(filter, queries);
    filter.setMaxContents(1);
    List<T> result = searchCms(filter, false);
    if (!result.isEmpty()) {
      return result.get(0);
    }
    return null;
  }

  public List<T> getList(List<QueryParameter> queries) {
    return searchCms(processQueryParams(getDefaultFilter(), queries), false);
  }

  protected long countResults(Filter filter) {
    filter.setStartFrom(0);
    filter.setMaxContents(0);
    SearchResult<Content> searchResult = SmartContentAPI.getInstance().getContentLoader().search(filter);
    return searchResult.getTotalResultsCount();
  }

  protected List<T> searchCms(Filter filter, boolean getAll) {
    if (getAll) {
      filter.setStartFrom(0);
      filter.setMaxContents(10);
    }
    SearchResult<Content> searchResult = SmartContentAPI.getInstance().getContentLoader().search(filter);
    final List<T> result;
    result = new ArrayList<T>();
    Collection<Content> collectionResult = searchResult.getResult();
    if (collectionResult != null && !collectionResult.isEmpty()) {
      result.addAll(adapter.convert(collectionResult.toArray(new Content[collectionResult.size()])));
    }
    if (getAll) {
      long totalResult = searchResult.getTotalResultsCount();
      long cTotal = result.size();
      if (totalResult > 10) {
        while (cTotal < totalResult) {
          filter.setStartFrom((int) cTotal);
          filter.setMaxContents(20);
          searchResult = SmartContentAPI.getInstance().getContentLoader().search(filter);
          collectionResult = searchResult.getResult();
          if (collectionResult != null && !collectionResult.isEmpty()) {
            result.addAll(adapter.convert(collectionResult.toArray(new Content[collectionResult.size()])));
            cTotal += collectionResult.size();
          }
          else {
            break;
          }
        }
      }
    }
    return result;
  }

  protected Filter processQueryParams(Filter filter,
                                      List<QueryParameter> queries) {
    filter.setFieldParamsEscaped(true);
    List<QueryParameter> cQueries = new ArrayList<QueryParameter>(queries);
    Iterator<QueryParameter> pIterator = cQueries.iterator();
    while (pIterator.hasNext()) {
      QueryParameter parameter = pIterator.next();
      switch (parameter.getParameterType()) {
        case PARAMETER_TYPE_MAX_RESULT: {
          ValueOnlyQueryParameter<Integer> param = QueryParameterCastHelper.VALUE_PARAM_HELPER.cast(parameter);
          filter.setMaxContents(param.getValue());
          pIterator.remove();
          break;
        }
        case PARAMETER_TYPE_FIRST_RESULT: {
          ValueOnlyQueryParameter<Integer> param = QueryParameterCastHelper.VALUE_PARAM_HELPER.cast(parameter);
          filter.setStartFrom(param.getValue());
          pIterator.remove();
          break;
        }
      }
    }
    List<QueryParameter> params = processQueryParams(cQueries, "", getContentTypeId().getContentType().getFieldDefs());
    filter.addFieldFilter(params.toArray(new QueryParameter[params.size()]));
    return filter;
  }

  protected List<QueryParameter> processQueryParams(final Collection<QueryParameter> queries,
                                                    final String prefix,
                                                    final Map<String, FieldDef> defs) {
    final List<QueryParameter> params = new ArrayList<QueryParameter>();
    for (QueryParameter parameter : queries) {
      switch (parameter.getParameterType()) {
        case PARAMETER_TYPE_CONJUNCTION: {
          BasicCompoundQueryParameter compoundQueryParameter = (BasicCompoundQueryParameter) parameter;
          final List<QueryParameter> conParams = processQueryParams(compoundQueryParameter.getNestedParameters(), prefix,
                                                                    defs);
          if (!conParams.isEmpty()) {
            params.add(
                QueryParameterFactory.getConjunctionParam(conParams.toArray(new QueryParameter[conParams.size()])));
          }
          break;
        }
        case PARAMETER_TYPE_DISJUNCTION: {
          BasicCompoundQueryParameter compoundQueryParameter = (BasicCompoundQueryParameter) parameter;
          final List<QueryParameter> disParams = processQueryParams(compoundQueryParameter.getNestedParameters(), prefix,
                                                                    defs);
          if (!disParams.isEmpty()) {
            params.add(
                QueryParameterFactory.getDisjunctionParam(disParams.toArray(new QueryParameter[disParams.size()])));
          }
          break;
        }
        case PARAMETER_TYPE_PROPERTY: {
          QueryParameterWithPropertyName para = (QueryParameterWithPropertyName) parameter;
          FieldDef def = defs.get(para.getPropertyName());
          final QueryParameter fieldQuery = addQueryForProperty(parameter, def, prefix);
          if (fieldQuery != null) {
            params.add(fieldQuery);
          }
          break;
        }
        case PARAMETER_TYPE_NESTED_PROPERTY: {
          CompositionQueryParameter para = QueryParameterCastHelper.COMPOSITION_PARAM_FOR_NESTED_TYPE.cast(parameter);
          FieldDef def = defs.get(para.getPropertyName());
          final DataType type;
          if (def.getValueDef().getType().equals(FieldValueType.COLLECTION)) {
            type = ((CollectionDataType) def.getValueDef()).getItemDataType();
          }
          else {
            type = def.getValueDef();
          }
          if (type.getType().equals(FieldValueType.CONTENT)) {
            ContentDataType dataType = (ContentDataType) type;
            final StringBuilder prefixBldr = new StringBuilder();
            if (StringUtils.isNotBlank(prefix)) {
              prefixBldr.append(prefix).append('_');
            }
            final String fieldName = SmartContentAPI.getInstance().getContentTypeLoader().
                getSearchFieldNameWithoutTypeSpecifics(def);
            if (StringUtils.isNotBlank(fieldName)) {
              params.addAll(processQueryParams(para.getNestedParameters(), prefixBldr.append(fieldName).toString(),
                                               dataType.getTypeDef().getContentType().getFieldDefs()));
            }
          }
          else if (type.getType().equals(FieldValueType.COMPOSITE)) {
            CompositeDataType dataType = (CompositeDataType) type;
            params.addAll(processQueryParams(para.getNestedParameters(), prefix, dataType.getComposedFieldDefs()));
          }
          break;
        }
        case PARAMETER_TYPE_ORDER_BY: {
          SimpleNameValueQueryParameter<Order> orderBy = QueryParameterCastHelper.SIMPLE_PARAM_HELPER.cast(parameter);
          final FieldDef def = defs.get(orderBy.getPropertyName());
          if (def != null) {
            String fieldName = SmartContentAPI.getInstance().getContentTypeLoader().getSearchFieldName(def);
            if (StringUtils.isNotBlank(fieldName)) {
              params.add(QueryParameterFactory.getOrderByParam(new StringBuilder(prefix).append(fieldName).
                  toString(), orderBy.getValue()));
            }
          }
          break;
        }
      }
    }
    return params;
  }

  protected QueryParameter addQueryForProperty(QueryParameter parameter, FieldDef def, final String prefix) {
    if (def != null) {
      String fieldName = SmartContentAPI.getInstance().getContentTypeLoader().getSearchFieldName(def);
      if (StringUtils.isNotBlank(fieldName)) {
        String searchFieldName = new StringBuilder(prefix).append(fieldName).toString();
        final String query;
        if (parameter instanceof StringLikeQueryParameter) {
          StringLikeQueryParameter queryParameter = (StringLikeQueryParameter) parameter;
          MatchMode mode = queryParameter.getMatchMode();
          if (mode == null) {
            mode = MatchMode.START;
          }
          switch (mode) {
            case START: {
              query = new StringBuilder(formatInSolrFormat(queryParameter.getValue())).append('*').toString();
              break;
            }
            case EXACT: {
              query = queryParameter.getValue();
              break;
            }
            case END: {
              query = new StringBuilder('*').append(formatInSolrFormat(queryParameter.getValue())).toString();
              break;
            }
            default:
            case ANYWHERE: {
              query =
              new StringBuilder('*').append(formatInSolrFormat(queryParameter.getValue())).append('*').toString();
              break;
            }
          }
        }
        else {
          query = generateQuery(parameter);
        }
        if (StringUtils.isNotBlank(query)) {
          final QueryParameter<String> stringLikePropertyParam =
                                       QueryParameterFactory.getStringLikePropertyParam(searchFieldName, query);
          return stringLikePropertyParam;
        }
      }
    }
    return null;
  }

  protected <T> String generateQuery(QueryParameter<T> creationDateFilter) {
    StringBuilder query = new StringBuilder();
    String dateQuery = "";
    switch (creationDateFilter.getParameterType()) {
      case PARAMETER_TYPE_PROPERTY:
        if (creationDateFilter instanceof UniOperandQueryParameter) {
          UniOperandQueryParameter<T> param =
                                      (UniOperandQueryParameter<T>) creationDateFilter;
          switch (param.getOperatorType()) {
            case OPERATOR_EQUAL:
              dateQuery = formatInSolrFormat(param.getValue());
              break;
            case OPERATOR_LESSER:
              query.insert(0, "NOT ");
              dateQuery = "[" + formatInSolrFormat(param.getValue()) + " TO *]";
//              dateQuery = "-[" + param.getValue() + " TO *]";
              break;
            case OPERATOR_GREATER_EQUAL:
              dateQuery = "[" + formatInSolrFormat(param.getValue()) + " TO *]";
              break;
            case OPERATOR_GREATER:
              query.insert(0, "NOT ");
              dateQuery = "[* TO " + formatInSolrFormat(param.getValue()) + "]";
//              dateQuery = "-[* TO " + param.getValue() + "]";
              break;
            case OPERATOR_LESSER_EQUAL:
              dateQuery = "[* TO " + formatInSolrFormat(param.getValue()) + "]";
              break;
            default:
              dateQuery = "[* TO *]";
          }
        }
        if (creationDateFilter instanceof BiOperandQueryParameter) {
          BiOperandQueryParameter<T> param =
                                     (BiOperandQueryParameter<T>) creationDateFilter;
          if (param.getOperatorType().equals(OperatorType.OPERATOR_BETWEEN)) {
            dateQuery = "[" + formatInSolrFormat(param.getFirstValue()) + " TO " + formatInSolrFormat(param.
                getSecondValue()) + "]";
          }
        }
        break;
      default:
        UniOperandQueryParameter<Date> param =
                                       (UniOperandQueryParameter<Date>) creationDateFilter;
        dateQuery = param.getPropertyName() + ": [* TO *]";
        break;
    }
    query.append(dateQuery);
    return query.toString();
  }

  protected <T> String formatInSolrFormat(T value) {
    if (value instanceof Date) {
      return formatInSolrFormat((Date) value);
    }
    if (value instanceof Integer) {
      return formatInSolrFormat((Integer) value);
    }
    if (value instanceof Long) {
      return formatInSolrFormat((Long) value);
    }
    if (value instanceof Double) {
      return formatInSolrFormat((Double) value);
    }
    if (value instanceof Boolean) {
      return formatInSolrFormat((Boolean) value);
    }
    if (value instanceof String) {
      return formatInSolrFormat((String) value);
    }
    return null;
  }

  protected String formatInSolrFormat(Date value) {
    return DateFormatUtils.formatUTC(value, SOLR_DATE_FORMAT);
  }

  protected String formatInSolrFormat(Integer value) {
    return String.valueOf(value);
  }

  protected String formatInSolrFormat(Long value) {
    return String.valueOf(value);
  }

  protected String formatInSolrFormat(Double value) {
    return String.valueOf(value);
  }

  protected String formatInSolrFormat(Boolean value) {
    return String.valueOf(value);
  }

  protected String formatInSolrFormat(String value) {
    return SmartContentAPI.getInstance().getContentLoader().escapeStringForSearch(value);
  }

  public <OtherTemplate> OtherTemplate getOther(List<QueryParameter> query) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public <OtherTemplate> List<OtherTemplate> getOtherList(List<QueryParameter> query) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public T getSingle(QueryParameter... query) {
    return getSingle(Arrays.asList(query));
  }

  public List<T> getList(QueryParameter... query) {
    return getList(Arrays.asList(query));
  }

  public <OtherTemplate> OtherTemplate getOther(QueryParameter... query) {
    return this.<OtherTemplate>getOther(Arrays.asList(query));
  }

  public <OtherTemplate> List<OtherTemplate> getOtherList(QueryParameter... query) {
    return this.<OtherTemplate>getOtherList(Arrays.asList(query));
  }

  public void save(T... states) {
    for (T state : states) {
      Content content = adapter.convertInversely(state);
      if (content != null) {
        WriteableContent wContent = SmartContentAPI.getInstance().getContentLoader().getWritableContent(content);
        try {
          wContent.put();
        }
        catch (Exception ex) {
          logger.warn("Could not save content ", ex);
          throw new RuntimeException(ex);
        }
      }
    }
  }

  public void update(T... states) {
    for (T state : states) {
      Content content = adapter.convertInversely(state);
      if (content != null) {
        WriteableContent wContent = SmartContentAPI.getInstance().getContentLoader().getWritableContent(content);
        try {
          wContent.put();
        }
        catch (Exception ex) {
          logger.warn("Could not save content ", ex);
          throw new RuntimeException(ex);
        }
      }
    }
  }

  public void delete(T... states) {
    for (T state : states) {
      Content content = adapter.convertInversely(state);
      if (content != null) {
        WriteableContent wContent = SmartContentAPI.getInstance().getContentLoader().getWritableContent(content);
        try {
          wContent.delete();
        }
        catch (Exception ex) {
          logger.warn("Could not save content ", ex);
          throw new RuntimeException(ex);
        }
      }
    }
  }

  public long count(QueryParameter... params) {
    return countResults(processQueryParams(getDefaultFilter(), Arrays.asList(params)));
  }
}
