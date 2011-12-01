var ids = args.id.split(",");
var token = args.token
var roots = [];
var rootPaths = [];

for ( var i = 0; i < ids.length; i++) {
	var r = search.findNode(ids[i].replace("\\s",""));
	roots.push(r);
	rootPaths.push(r.displayPath) + '/' + r.name;
}
var session = cmis.connection.getSession();
session.clear();
var changeEvents = session.getContentChanges(token, true, 99999);
var events = changeEvents.getChangeEvents();

var myEvents = [];

for ( var i = 0; i < events.size(); i++) {
	var ce = events.get(i);
	var noderef = ce.getObjectId().split(";")[0];
	var uuid = noderef.substring(24);

	var type = ('' + ce.getChangeType()).substring(0, 1);

	if (type.equals('D')) {
		myEvents.push([ type, uuid ]);
	} else {
		node = search.findNode(noderef);
		var nt = node.getTypeShort();
		if (node == null || (nt != 'cm:content' && nt != 'cm:folder')) {
			continue;
		}
		for ( var j = 0; j < rootPaths.length; j++) {
			if (node.displayPath.startsWith(rootPaths[j])) {
				myEvents.push([ type, uuid ]);
				break;
			}
		}

	}

}

model.events = myEvents;
model.token = session.getBinding().getRepositoryService().getRepositoryInfo(
		session.getRepositoryInfo().getId(), null).getLatestChangeLogToken();
