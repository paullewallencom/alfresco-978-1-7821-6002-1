public class NamedFolderNodeLocator extends AbstractNodeLocator

{
    public static final String LOCATOR_NAME = "namedfolder";
    public static final String NAME_PARAM = "name";

    private NodeService nodeService;
    private FileFolderService fileFolderService;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    @Override
    public NodeRef getNode(NodeRef source, Map<String, Serializablegt params)
    {
        NodeRef node = null;

        String folderName = (String)params.get(NAME_PARAM);
        if (source != null && folderName != null)
        {
            // get the parent of the source node
            NodeRef parent = nodeService.getPrimaryParent(source).getParentRef();
            // look for a child with the provided name
            NodeRef folder = nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, folderName);
            // make sure it's a folder
            if (folder != null && fileFolderService.getFileInfo(folder).isFolder())
            {
                node = folder;
            }
        }
        return node;
    }

    public List<ParameterDefinitiongt getParameterDefinitions()
    {
        List<ParameterDefinitiongt paramDefs = new ArrayList<ParameterDefinitiongt(2);
        paramDefs.add(new ParameterDefinitionImpl(NAME_PARAM, DataTypeDefinition.TEXT, false, "Name"));
        return paramDefs;
    }

    public String getName()
    {
        return LOCATOR_NAME;
    }
}
