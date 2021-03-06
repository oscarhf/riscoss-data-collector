package eu.riscoss.rdc;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import eu.riscoss.dataproviders.RiskData;
import eu.riscoss.dataproviders.RiskDataType;

public class RDCGithub implements RDC {
	
	static Set<String>				names = new HashSet<>();
	static Map<String,RDCParameter>	parameters = new HashMap<>();
	
	Map<String,String> values = new HashMap<>();
	
	static {
		names.add( "forks" );
		names.add( "open_issues_count" );
		names.add( "stargazers_count" );
		names.add( "created_at" );
		names.add( "subscribers_count" );
		names.add( "open_issues" );
		names.add( "watchers_count" );
		names.add( "forks_count" );
		names.add( "size" );
		names.add( "has_wiki" );
		names.add( "updated_at" );
		
		parameters.put( "repository", new RDCParameter( "repository", "Repository name", "RISCOSS/riscoss-analyser", null ) );
	}
	
	@Override
	public String getName() {
		return "Github";
	}
	
	@Override
	public Collection<RDCParameter> getParameterList() {
		return parameters.values();
	}
	
	@Override
	public void setParameter(String parName, String parValue) {
		values.put( parName, parValue );
	}
	
	@Override
	public Map<String, RiskData> getIndicators() {
		
		String repository = values.get( "repository" );
		
		String cmdline = "curl -H \"Accept: application/vnd.github.drax-preview+json\" -i https://api.github.com/repos/" + repository;
		String json = new Executable( cmdline ).exec().getOutput();
		
		return parseJson( json.substring( json.indexOf( "{" ) ), repository );
	}
	
	Map<String,RiskData> parseJson( String json, String repository ) {
		Map<String,RiskData> values = new HashMap<>();
		try {
			try {
				JSONAware jv = (JSONAware) new JSONParser().parse( json );
				if( jv instanceof JSONObject ) {
					JSONObject jo = (JSONObject)jv;
					for( Object key : jo.keySet() ) {
						if( RDCGithub.names.contains( key.toString() ) ) {
							
							String value = jo.get( key ).toString();
							try {
								double d = Double.parseDouble( value );
								RiskData rd = new RiskData( key.toString(), repository, new Date(), RiskDataType.NUMBER, d );
								values.put( key.toString(), rd );
							}
							catch( Exception ex ) {}
						}
					}
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return values;
	}

	@Override
	public Collection<String> getIndicatorNames() {
		return names;
	}
}
