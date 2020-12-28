private static String buildCmisQuery(ArrayList<Property> param, Object user, String urlPlatform, String TICKET) throws UnsupportedEncodingException{
		try{
			Log.getLogger().trace("Building CMIS query standard");
			ApplicationContext ctx = ApplicationContextProvider.getApplicationContext();
			TypeLoader loader = (TypeLoader) ctx.getBean("types");
			HashMap<String, HashMap<String, Object>> types = loader.getTypes();
			GlobalProperties global = (GlobalProperties) ctx.getBean("globalProperties");
			String maxItem = global.getMaxItemCmis();
			
			ArrayList<HashMap> aspectList = new ArrayList<HashMap>();
			String where = "";
			String select = "";
			String tipoDoc = "cmis:document";
			String aliasList = "";
			// Parse date value
			String dateValue = "";
			for(int i=0 ; i<param.size() ; i++){	
		        	if(param.get(i).getValue() != null && param.get(i).getValue().length()>0){
		        		String value = param.get(i).getValue();
		        		if(where.length()==0){
							// A custom modifier for a value input where a year is written with YY or YYYY format
							if(value.length() == 2)
			        				where += " WHERE (" + param.get(i).getAlias() + "." + param.get(i).getCmis() + " = '" + value + "' OR " + param.get(i).getAlias() + "." + param.get(i).getCmis() + " = '20" + value + "')";
			        			else
			        				where += " WHERE (" + param.get(i).getAlias() + "." + param.get(i).getCmis() + " = '" + value + "' OR " + param.get(i).getAlias() + "." + param.get(i).getCmis() + " = '" + value.substring(2) + "')";
						}
					// A custom query modifier if SELECT string has already some values or is == 0
		        	if(select.length() == 0){
		        		Log.getLogger().trace("BUILDING QUERY : "+param.get(i).getAlias()+" - "+aliasList);
		        		if((aliasList.contains(param.get(i).getAlias()) || "z".equals(param.get(i).getAlias())) && param.get(i).getAlias().length()>0)
		        			select += " SELECT z.cmis:objectId, z.cmis:objectTypeId,  " + param.get(i).getAlias() + "." + param.get(i).getCmis();
		        	}else{
		        		Log.getLogger().trace("BUILDING QUERY : "+param.get(i).getAlias()+" - "+aliasList + " - "+param.get(i).getId());
		        		if((aliasList.contains(param.get(i).getAlias()) || "z".equals(param.get(i).getAlias())) && param.get(i).getAlias().length()>0)
		        			select += " , " + param.get(i).getAlias() + "." + param.get(i).getCmis();
		        	}	        	

