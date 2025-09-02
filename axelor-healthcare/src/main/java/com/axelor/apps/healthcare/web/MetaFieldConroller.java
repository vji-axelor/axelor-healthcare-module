package com.axelor.apps.healthcare.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.inject.Beans;
import com.axelor.meta.CallMethod;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.schema.views.Selection.Option;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetaFieldConroller {

  @SuppressWarnings({"unchecked", "rawtypes"})
  @CallMethod
  public void getMetaFields(ActionRequest request, ActionResponse response) {
    try {
      Map<String, Object> data = request.getData();
      String model = (String) data.get("domain");
      Class<Model> klass = (Class<Model>) Class.forName(model);
      List<Map> metaFields = getMetaFieldList(klass, model);
      response.setValue("fields", metaFields);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("rawtypes")
  public List<Map> getMetaFieldList(Class<Model> klass, String model) throws Exception {

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

      int lastDot = model.lastIndexOf(".");
      String packageName = model.substring(0, lastDot);
      String modelName = model.substring(lastDot + 1);

      MetaField metaField = null;
      String relationalModel = "";
      if (property.getType().toString().equals("ONE_TO_MANY")
          || property.getType().toString().equals("MANY_TO_ONE")
          || property.getType().toString().equals("MANY_TO_MANY")
          || property.getType().toString().equals("ONE_TO_ONE")) {

        metaField =
            Beans.get(MetaFieldRepository.class)
                .all()
                .filter(
                    "self.name = ? AND self.metaModel.name = ? AND self.metaModel.packageName = ?",
                    property.getName(),
                    modelName,
                    packageName)
                .fetchOne();
        if (metaField != null) {
          relationalModel = metaField.getPackageName() + "." + metaField.getTypeName();
        }
      }

      map.put("field", property.getName());
      map.put("type", property.getType());
      map.put("require", property.isRequired());
      map.put("title", property.getTitle());
      map.put("relational", property.isReference());
      map.put("relationalModel", relationalModel);

      metaFieldNames.add(map);
    }

    return metaFieldNames;
  }
}
