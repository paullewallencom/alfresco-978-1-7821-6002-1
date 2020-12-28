$(document).ready(function() {

			var username = "${person.properties.userName}"; 
			var taskListURL = "/alfresco/wcs/api/task-instances.json?authority=" + username; 
	 		// create a template using the above definition

	      	var template = kendo.template($("#template").html());

			//$("#test").kendoAutoComplete();

			var ds = new kendo.data.DataSource({
			    transport: {
			        read: {
			          url: taskListURL, // the remote service url
			          dataType: "json", // JSONP (JSON with padding) is required for cross-domain AJAX
			     	}
			    },
				schema: {
			      	data: function(response) {
						return response.data; // alfresco wf istance response is { "data": [ /* results */ ] }
						}
				},
			    change: function() { // subscribe to the CHANGE event of the data source
			        $("#assignedworkflows tbody").html(kendo.render(template, this.view())); // populate the table
			    },
			    columns: [
					{ field: "workflowInstance.title" },
					{ field: "workflowInstance.message" },
					{ field: "workflowInstance.initiator.userName" },
					{ field: "workflowInstance.startDate" },
					{ field: "workflowInstance.dueDate" },
					{ field: "workflowInstance.package" },
					{ field: "workflowInstance.id" },
					{ field: "properties.bpm_taskId" },
					{ field: "state" },
					{ field: "properties.wf_reviewOutcome" }

				]
			});
}


