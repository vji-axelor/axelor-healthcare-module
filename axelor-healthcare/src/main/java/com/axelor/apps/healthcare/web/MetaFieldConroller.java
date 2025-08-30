package com.axelor.apps.healthcare.web;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.CallMethod;
import com.axelor.meta.MetaStore;
import com.axelor.meta.schema.views.Selection.Option;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.api.client.util.Strings;

public class MetaFieldConroller {

	  @SuppressWarnings({"unchecked", "rawtypes"})
	  @CallMethod
	  public void getMetaFields(ActionRequest request, ActionResponse response) {
	    try {
	      Map<String, Object> data = request.getData();
	      String model = (String) data.get("domain");
	      Class<Model> klass = (Class<Model>) Class.forName(model);
	      List<Map> metaFields = getMetaFieldList(klass);
	      response.setValue("fields", metaFields);
	    } catch (Exception e) {
	      TraceBackService.trace(response, e);
	    }
	  }
	  
	  
	  @SuppressWarnings("rawtypes")
	  public List<Map> getMetaFieldList(Class<Model> klass) throws Exception {

	    List<Map> metaFieldNames = new ArrayList<>();
	    Mapper mapper = Mapper.of(klass);

	    for (Property property : mapper.getProperties()) {

	      Map<String, Object> map = new HashMap<>();

	      if (!Strings.isNullOrEmpty(property.getSelection())) {
	        List<Option> selectionList = MetaStore.getSelectionList(property.getSelection());
	        Map<String, Object> selectionItem = new HashMap<>();

	        for (Option item : selectionList) {
	          selectionItem.put(item.getValue(), item.getTitle());
	        }
	        map.put("selectionItem", selectionItem);
	      } else {
	        map.put("selectionItem", null);
	      }

	      map.put("field", property.getName());
	      map.put("type", property.getType());
	      map.put("require", property.isRequired());
	      map.put("title", property.getTitle());
	      map.put("relational", property.isReference());

	      metaFieldNames.add(map);
	    }

	    return metaFieldNames;
	  }
}
