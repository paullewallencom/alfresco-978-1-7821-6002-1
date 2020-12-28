package com.test.me.testopencmis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

public class TestOpenCmis {
	public static void main(String[] args) {

	//		TEST OpenCMIS connection
	String cmisUrl = "http://localhost:8080/alfresco/service/cmis";
	
	// set up session parameters
			Map<String, String> parameter = new HashMap<String, String>();
			parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
			parameter.put(SessionParameter.ATOMPUB_URL, cmisUrl);
			parameter.put(SessionParameter.USER, "admin");
			parameter.put(SessionParameter.PASSWORD, "admin");
			
			// Set the alfresco object factory
			parameter.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");

			// create the OpenCMIS session
			SessionFactory factory = SessionFactoryImpl.newInstance();	
			List<Repository> repositories = factory.getRepositories(parameter);
			Session session = repositories.get(0).createSession();		
	
			Folder root = session.getRootFolder();
			CmisObject object = session.getObject(root);
			object.refresh();
			ItemIterable<CmisObject> children = root.getChildren();

			System.out.println("Found the following objects in the root folder:-");
			for (CmisObject o : children) {
			    System.out.println(o.getName() + " which is of type " + o.getType().getDisplayName() + " and has id: " + o.getId());
			}
//	END TEST
	}
}
