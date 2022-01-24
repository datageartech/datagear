由codemirror-5.64.0/theme/idea.css修改：

1.
"idea" 替换为 "custom"

2.
.CodeMirror-hints.custom {
  font-family: Menlo, Monaco, Consolas, 'Courier New', monospace;
  color: #616569;
  background-color: #ebf3fd !important;
}

.CodeMirror-hints.custom .CodeMirror-hint-active {
  background-color: #a2b8c9 !important;
  color: #5c6065 !important;
}

替换为

.CodeMirror-hints.custom {
  background-color: #fff !important;
  border: 1px solid #dddddd;
  color: #000;
  -webkit-box-shadow: 0px 0px 5px #666666;
  box-shadow: 0px 0px 5px #666666;
}
.CodeMirror-hints.custom .CodeMirror-hint{
	color: #000;
}
.CodeMirror-hints.custom .CodeMirror-hint-active {
  background-color: #ededed !important;
  color: #000 !important;
}

3.
.cm-s-custom .CodeMirror-matchingbracket { outline:1px solid grey; color:black !important; }

替换为

.cm-s-custom .CodeMirror-matchingbracket { outline:1px solid #CCC; color:black !important; }