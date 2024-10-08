---
sidebar_position: 4
---

# Sentence Lexer
Gestalt uses a SentenceLexer's in several places, to convert a string path into tokens that can be followed and to in the ConfigParser to turn the configuration paths into tokens then into config nodes.
You can customize the SentenceLexer to use your own format of path. For example in Gestalt Environment Variables use a '_' to delimitate the tokens whereas property files use '.'. If you wanted to use camel case you could build a sentence lexer for that.
