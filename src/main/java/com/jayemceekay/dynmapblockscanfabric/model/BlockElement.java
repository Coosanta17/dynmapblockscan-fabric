package com.jayemceekay.dynmapblockscanfabric.model;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.jayemceekay.dynmapblockscanfabric.BlockScanCore;
import com.jayemceekay.dynmapblockscanfabric.blockstate.ElementFace;
import com.jayemceekay.dynmapblockscanfabric.blockstate.ModelRotation;


// Container for parsed JSON elements from block model
public class BlockElement {
    public double[] from;
    public double[] to;
    public ElementRotation rotation = null;
    public Map<ElementFace, BlockFace> faces = Collections.emptyMap();
    public boolean shade = true;
    public boolean uvlock = false;

    public ModelRotation modelrot = new ModelRotation(0, 0, 0);	// Model rotation inherited from block state

    public BlockElement() {}
    
    /**
     * Create copy of BlockModel with texture references resolved from
     * provided model
     * @param src - source elements
     * @param txtrefs - source of texture references
     */
    public BlockElement(BlockElement src, TextureReferences txtrefs, ModelRotation mrot, boolean uvlock) {
    	from = Arrays.copyOf(src.from, src.from.length);
    	to = Arrays.copyOf(src.to, src.to.length);
    	if (src.rotation != null) { 
    		rotation = new ElementRotation(src.rotation);
    	}
    	if (mrot != null) {
    		modelrot = mrot;
    	}
    	shade = src.shade;
    	// Build resolved copies of faces
    	faces = new HashMap<ElementFace, BlockFace>();
    	for (Entry<ElementFace, BlockFace> face : src.faces.entrySet()) {
    		BlockFace f = face.getValue();
    		String v = txtrefs.findTextureByID(f.texture);	// Resolve texture
    		if (v == null) {	
    			BlockScanCore.logger.info("Unresolved texture ref: " + f.texture);
    			BlockScanCore.logger.info(txtrefs.toString());
    		}
    		else {
    			faces.put(face.getKey(), new BlockFace(f, v, 0));
    		}
    	}        
        this.uvlock = uvlock;   // Remember uvlock
    }
    
    // Test if element is simple cuboid (grid aligned)
    public boolean isSimpleCuboid() {
        // If rotation is zero
        if ((rotation != null) && (rotation.angle != 0.0)) {
            return false;
        }
        return true;
    }
    
    // Test if element is simple, full block
    public boolean isSimpleBlock() {
        // Must be simple cuboid
        if (!isSimpleCuboid()) {
            return false;
        }
    	if (!this.modelrot.isDefault()) {
    		return false;
    	}
    	// Check from corner
    	if ((from == null) || (from.length < 3) || (from[0] != 0.0F) || (from[1] != 0.0F) || (from[2] != 0.0F)) {
    		return false;
    	}
    	// Check to corner
    	if ((to == null) || (to.length < 3) || (to[0] != 16.0F) || (to[1] != 16.0F) || (to[2] != 16.0F)) {
    		return false;
    	}
    	// Number of faces
    	for (ElementFace f : ElementFace.values()) {
    		BlockFace ff = faces.get(f);
    		if ((ff == null) || (ff.isFullFace() == false)) {
    			return false;
    		}
    	}
    	return true;
    }
    
	// Custom deserializer - handles singleton and list formats
    public static class Deserializer implements JsonDeserializer<BlockElement> {
    	@Override
        public BlockElement deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
    		BlockElement be = new BlockElement();
    		JsonObject obj = element.getAsJsonObject();
    		if (obj.has("from")) {
    			be.from = context.deserialize(obj.get("from"), double[].class);
    		}
    		if (obj.has("to")) {
    			be.to = context.deserialize(obj.get("to"), double[].class);
    		}
    		if (obj.has("rotation")) {
    			be.rotation = context.deserialize(obj.get("rotation"), ElementRotation.class);
    		}
    		if (obj.has("faces")) {
    			JsonObject f = obj.get("faces").getAsJsonObject();
    			be.faces = new HashMap<ElementFace, BlockFace>();
    			for (Entry<String, JsonElement> fe : f.entrySet()) {
    				ElementFace facing = ElementFace.byFace(fe.getKey());
    				if (facing != null) {
    					be.faces.put(facing, context.deserialize(fe.getValue(), BlockFace.class));
    				}
    			}
    		}
    		if (obj.has("shade")) {
    			be.shade = obj.get("shade").getAsBoolean();
    		}
            return be;
        }
    }

}
