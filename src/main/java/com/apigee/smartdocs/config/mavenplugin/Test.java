package com.apigee.smartdocs.config.mavenplugin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;

import com.apigee.smartdocs.config.rest.PortalRestUtil;
import com.apigee.smartdocs.config.utils.PortalField;
import com.apigee.smartdocs.config.utils.ServerProfile;
import com.google.gson.internal.LinkedTreeMap;

public class Test {
	  private static File[] files = null;
	  
	public static void main(String[] args) throws MojoExecutionException {
		// TODO Auto-generated method stub
		ServerProfile serverProfile = new ServerProfile();
		serverProfile.setPortalURL("http://dev-amer-poc20.devportal.apigee.io");
		serverProfile.setPortalPath("smartdocs/apis");
		serverProfile.setPortalUserName("ssvaidyanathan@google.com");
		serverProfile.setPortalPassword("apigee123");
		serverProfile.setPortalDirectory("/Users/saisaranvaidyanathan/GitHub/apigee-smartdocs-maven-plugin/samples/DevPortal/specs");
		serverProfile.setPortalFormat("yaml");
		String directory = serverProfile.getPortalDirectory();
	    files = new File(directory).listFiles();
		
		Map<String, PortalField> modelFields = new HashMap<String, PortalField>();	
		PortalField p = new PortalField();
		p.setField("field_country"); 
		p.setPath("contact|country");
		modelFields.put("country", p);
		serverProfile.setPortalModelFields(modelFields);
		serverProfile.setPortalModelVocabulary("smartdocs_models");
		try {
			PortalRestUtil.VocabularyObject vo = PortalRestUtil.getVocabulary(serverProfile, serverProfile.getPortalModelVocabulary());
			Collection<PortalRestUtil.TaxonomyTermObject> tos = PortalRestUtil.getTaxonomyTerms(serverProfile, vo.vid);
			
			for (File file : files) {
		          PortalRestUtil.SpecObject spec = PortalRestUtil.parseSpec(serverProfile, file);
		          for (PortalRestUtil.TaxonomyTermObject to: tos) {
		            // Match file and taxonomy term.
		            if (to.name.equals(spec.getName())) {
		              HashMap hs = new HashMap();
		              for (PortalField pf : modelFields.values()) {
		                // Elements can be embedded within the info object, so
		                // find the location, and extract the value.
		                String[] pathParts = pf.getPath().split("\\|");
		                LinkedTreeMap jo = spec.info;
		                for (String pathPart : pathParts) {
		                  // Only traverse down if the key exists.
		                  if (jo.containsKey(pathPart)) {
		                    Object o = jo.get(pathPart);
		                    // If we still need to go deeper, we have a tree.
		                    if (o instanceof LinkedTreeMap) {
		                      jo = (LinkedTreeMap)o;
		                    }
		                    else {
		                      // Otherwise, store the value in our hash.
		                      hs.put(pf.getField(), o.toString());
		                    }
		                  }
		                  else {
		                    // If we ever fail to find an item, break out.
		                    break;
		                  }
		                }
		              }
		              
		              if (!hs.isEmpty()) {
		                // If we have values, set keys and push the update.
		                hs.put("tid", to.tid);
		                hs.put("vid", to.vid);
		                hs.put("name", to.name);
		                hs.put("uuid", to.uuid);
		                PortalRestUtil.updateTaxonomyTerm(serverProfile, hs);
		              }
		              // Remove our matched term so we don't have to check it again.
		              tos.remove(to);
		              break;
		            }
		          } // End loop over taxonomy terms (models).
		        }
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*HashMap hs = new HashMap<String, String>();
		hs.put("tid", "75");
        hs.put("vid", "5");
        hs.put("name", "Mock-Target-API");
        hs.put("field_country", "{company=Test Company}");
		try {
			PortalRestUtil.updateTaxonomyTerm(serverProfile, hs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
	}
}
