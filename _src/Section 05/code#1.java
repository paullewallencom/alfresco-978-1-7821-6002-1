public static String retrieveTicket(Piattaforma platform){
	String ticketUrl = platform.getTicketUrl();
	ticketUrl = ticketUrl.replace("%USER%", platform.getUser());
	ticketUrl = ticketUrl.replace("%PWD%", platform.getPassword());
	String url = "http://" + platform.getIp() + ":" + platform.getPort() + "/" + platform.getContext() + "/" + ticketUrl;
	String ticket = "";
	try{
		URL urlAuthent = new URL(url);
		URLConnection connAuth = urlAuthent.openConnection();
		connAuth.setConnectTimeout(Integer.parseInt(platform.getTimeout()));
		InputStream in = connAuth.getInputStream();
		Document xml = Utils.readXml(in);
		NodeList nl = xml.getElementsByTagName("ticket");			
		if(nl.getLength() > 0){
			ticket = nl.item(0).getTextContent();
		}			
		Log.getLogger().trace("Platform Ticket : "+platform.getId());
	}catch(IOException e){
		Log.getLogger().error(e);
	} catch (SAXException e) {
		// TODO Auto-generated catch block
		Log.getLogger().error(e);
	} catch (ParserConfigurationException e) {
		// TODO Auto-generated catch block
		Log.getLogger().error(e);
	}
	return ticket;
}

