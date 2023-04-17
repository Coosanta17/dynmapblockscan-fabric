package com.jayemceekay.dynmapblockscanfabric.blockstate;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.jayemceekay.dynmapblockscanfabric.AbstractBlockScanBase;
import com.jayemceekay.dynmapblockscanfabric.model.BlockElement;
import com.jayemceekay.dynmapblockscanfabric.model.BlockModel;

// Container for parsed JSON encoding of Variant from blockstate
public class Variant {
	public String model = "cube";
	public ModelRotation rotation = new ModelRotation(0, 0, 0);
	public boolean uvlock = false;
	public int weight = 1;
	
	// Variant list from submodels, if any
	public VariantList subvariants;
	
	// Normalized model ID
	public String modelID = "cube";
	// Generated and resolved element list
	public List<BlockElement> elements;
	
	public Variant() {
	}
	
	public Variant(String mod, Integer x, Integer y, Integer z, Boolean uv, Integer wt) {
	    this.model = this.modelID = mod;
	    this.rotation = ModelRotation.getModelRotation((x != null)?x:0, (y != null)?y:0, (z != null)?z:0);
	    if (wt != null) {
	        this.weight = wt;
	    }
	    if (uv != null) {
	        this.uvlock = uv;
	    }
	}
	// Generate and resolve element list for variant
	public boolean generateElements(Map<String, BlockModel> models) {
	    elements = new ArrayList<BlockElement>();
		BlockModel basemod = models.get(modelID);	// Loop up our model
		if (basemod != null) {
		    // Find topmost elements
		    BlockModel elemmod = basemod;
		    while (elemmod.elements.isEmpty()) {
				// Assert: if parent is defined, needs to have parentModel
				if ((basemod.parent != null) && (elemmod.parentModel == null)) {
					AbstractBlockScanBase.logger.info("ERROR: modelID=" + modelID + " has parent=" + basemod.parent + " but no parentModel");
				}
		        elemmod = elemmod.parentModel;	// Get parent model
		        if (elemmod == null) {	// Not found, we've got no elements
		            break;
		        }
		    }
		    // Now, we're going to build copies of the elements with resolved models
		    if (elemmod != null) {
		        for (BlockElement elem : elemmod.elements) {
		            elements.add(new BlockElement(elem, basemod, rotation, uvlock));
		        }
		    }
		}
		// If we have subvariants (from forge submodels), add these too
		if (this.subvariants != null) {
		    for (Variant subv : this.subvariants.variantList) {
		        subv.generateElements(models);    // Generate their elements
		        if (subv.elements != null) {
		            elements.addAll(subv.elements);   // And add them all to ours
		        }
		    }
		}
		return (elements.size() > 0);
	}
	
	@Override
	public int hashCode() {
		return model.hashCode() ^ rotation.rotX ^ rotation.rotY ^ rotation.rotZ ^ (weight << 16) ^ (uvlock?12345:0);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj instanceof Variant) {
			Variant vobj = (Variant) obj;
			return (model.equals(vobj.model) && (rotation == vobj.rotation) && (uvlock == vobj.uvlock) && (weight == vobj.weight));
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{");
		sb.append("model=").append(model);
		sb.append(",rotation=").append(rotation);
		if (uvlock)
			sb.append(",uvlock=true");
		if (weight != 1)
			sb.append(",weight=").append(weight);
		sb.append("}");
		return sb.toString();
	}

	// Custom deserializer - handles singleton and list formats
    public static class Deserializer implements JsonDeserializer<Variant> {
    	@Override
        public Variant deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
    		Variant var = new Variant();
    		JsonObject obj = element.getAsJsonObject();
    		
    		if (obj == null) {
                return var;
    		}
            int x = 0;
            int y = 0;
            int z = 0;
            if (obj.has("x")) {
                x = obj.get("x").getAsInt();
            }
            if (obj.has("y")) {
                y = obj.get("y").getAsInt();
            }
            if (obj.has("z")) {
                z = obj.get("z").getAsInt();
            }
            var.rotation = ModelRotation.getModelRotation(x, y, z);
    		if (obj.has("model"))
    		    var.model = obj.get("model").getAsString();
    		else
    		    var.model = "cube";   // Default?
    		var.modelID = var.model;
    		if (obj.has("uvlock")) {
    			var.uvlock = obj.get("uvlock").getAsBoolean();
    		}
    		if (obj.has("weight")) {
    			var.weight = obj.get("weight").getAsInt();
    		}
            return var;
        }
    }

}
