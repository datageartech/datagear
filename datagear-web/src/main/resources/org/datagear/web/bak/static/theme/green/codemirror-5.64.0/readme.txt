由codemirror-5.64.0/theme/cobalt.css修改：

1.
"cobalt" 替换为 "custom"

2.
"#002240" 替换为 "#285c00"

3.
"#3ad900" 替换为 "#f9f951"

4.
新增

.CodeMirror-hints.custom {
  background: #285c00 !important;
	border: 1px solid #45930b;
    color: #FFFACD !important;
    -webkit-box-shadow: 0px 0px 6px #cccccc;
    box-shadow: 0px 0px 6px #cccccc;
}
.CodeMirror-hints.custom .CodeMirror-hint{
	color: #FFFACD;
}
.CodeMirror-hints.custom .CodeMirror-hint-active {
  background-color: #459e05 !important;
  color: #FFFACD !important;
}
.cm-s-custom  { font-family: Consolas, Menlo, Monaco, Lucida Console, Liberation Mono, DejaVu Sans Mono, Bitstream Vera Sans Mono, Courier New, monospace, serif;}
.CodeMirror-scrollbar-filler, .CodeMirror-gutter-filler{
	background-color: #285c00;
}

.CodeMirror-foldmarker{
	color: #FFF;
}