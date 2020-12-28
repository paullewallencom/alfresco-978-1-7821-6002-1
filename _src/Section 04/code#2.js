var nodes;

if (args.type == "doc1")
{
   //if year is present
   if (args.year != undefined) {
		var queryString = "TYPE:\"customUri:doc1\" AND +@customUri\\:numdoc:\"" + args.numdoc +"\" AND +@customUri\\:year:\"*"+ args.year +"*\"";
		nodes = search.luceneSearch(queryString);
	}
   //if year is missing
	else {
		var queryString = "TYPE:\"customUri:doc1\" AND +@customUri\\:numdoc:\"" + args.numdoc +"\"";
		nodes = search.luceneSearch(queryString, "@cm:created", false);
	}
}

if (args.type == "doc2")
{
   //if year is present
   if (args.year != undefined) {
		var queryString = "TYPE:\"customUri:doc2\" AND +@customUri\\:numdoc:\"" + args.numdoc +"\" AND +@customUri\\:year:\"*"+ args.year +"*\"";
		nodes = search.luceneSearch(queryString);
	}
   //if year is missing
	else {
		var queryString = "TYPE:\"customUri:doc2\" AND +@customUri\\:numdoc:\""+ args.numdoc +"\"";
		nodes = search.luceneSearch(queryString, "@cm:created", false);
	}
}

status.message = "Success";
model.resultset = nodes;
    model.node= nodes[0];
model.type = args.type;
model.year = args.year;

