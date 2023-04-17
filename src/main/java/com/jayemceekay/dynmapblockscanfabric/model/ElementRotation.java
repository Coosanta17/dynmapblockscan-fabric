package com.jayemceekay.dynmapblockscanfabric.model;

import java.util.Arrays;


public class ElementRotation {
	public double[] origin = { 8.0f, 8.0f, 8.0f };
	public String axis = "x";
	public double angle = 0.0f;
	public boolean rescale = false;
	
	public ElementRotation() {}
	
	public ElementRotation(ElementRotation src) {
		origin = Arrays.copyOf(src.origin, src.origin.length);
		axis = src.axis;
		angle = src.angle;
		rescale = src.rescale;
	}
}
