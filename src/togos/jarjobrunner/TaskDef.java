package togos.jarjobrunner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TaskDef
{
	public static final Object[] emptyArgumentArray = new Object[0];
	
	public final String jarUri;
	public final String className;
	public final Object[] additionalConstructorArguments;
	
	public TaskDef( String jarUri, String className, Object[] constructorInputs ) {
		assert jarUri != null;
		assert className != null;
		assert constructorInputs != null;
		
		this.jarUri = jarUri;
		this.className = className;
		this.additionalConstructorArguments = constructorInputs;
	}
	
	static TaskDef fromJson( JSONObject js ) throws JSONException {
		Object[] constructorArgs;
		if( js.has("additionalConstructorArguments") ) {
			JSONArray caja = js.getJSONArray("additionalConstructorArguments");
			constructorArgs = new Object[caja.length()];
			for( int i=0; i<caja.length(); ++i ) {
				constructorArgs[i] = caja.get(i);
			}
		} else {
			constructorArgs = emptyArgumentArray;
		}
		
		return new TaskDef( js.getString("jarUri"), js.getString("className"), constructorArgs );
	}
	static TaskDef fromJson( String js ) throws JSONException {
		return fromJson( new JSONObject(js) );
	}
	
	protected boolean arraysEqual( Object[] a1, Object[] a2 ) {
		if( a1.length != a2.length ) return false;
		
		for( int i=0; i<a1.length; ++i ) {
			if( a1 == a2 ) continue;
			if( a1 == null || a2 == null ) return false;
			if( !a1[i].equals(a2[i]) ) return false;
		}
		
		return true;
	}
	
	@Override public boolean equals(Object obj) {
		if( !(obj instanceof TaskDef) ) return false;
		TaskDef td = (TaskDef)obj;
		return jarUri.equals(td.jarUri) && className.equals(td.className) && arraysEqual( additionalConstructorArguments, td.additionalConstructorArguments );
	}
}
