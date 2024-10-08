---
sidebar_position: 5
---

# Reload Strategy
You are able to reload a single source and rebuild the config tree by implementing your own ConfigReloadStrategy.


## ConfigNodeService
The ConfigNodeService is the central storage for the merged config node tree along with holding the original config nodes stored in a ConfigNodeContainer with the original source id. This is so when we reload a config source, we can link the source being reloaded with the config tree it produces.
Gestalt uses the ConfigNodeService to save, merge, result the config tree, navigate and find the node Gestalt is looking for.


## Gestalt
The external facing portion of Java Config Library, it is the keystone of the system and is responsible for bringing together all the pieces of project. Since Gestalt relies on multiple services, the Builder makes it simple to construct a functional and default version of Gestalt.


### loadConfigs
The built Gestalt is used to load the config sources by adding them to the builder and then passed through to the Gestalt constructor.
Gestalt will use the ConfigLoaderService to find a ConfigLoader that will load the source by a format. It will add the config node tree loaded to the ConfigNodeService to be added with the rest of the config trees. The new config tree will be merged and where applicable overwrite any of the existing config nodes.


### reload
When a source needs to be reloaded, it will be passed into the reload function. The sources will then be converted into a Config node as in the loading. Then Gestalt will use the ConfigNodeService to reload the source. Since the ConfigNodeService holds onto the source ID with the ConfigNodeContainer we are able to determine with config node to reload then take all the config nodes and re-merge them in the same order to rebuild the config tree with the newly loaded node.
