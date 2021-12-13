由codemirror-5.64.0/theme/ayu-dark.css修改：

1.
"ayu-dark" 替换为 "custom"

2.
"#0a0e14" 替换为 "#000"

3.
新增

.CodeMirror-hints.custom {
  background: #000 !important;
  border: 1px solid #666666;
  color: #FFDEAD !important;
  -webkit-box-shadow: 0px 0px 6px #cccccc;
  box-shadow: 0px 0px 6px #cccccc;
}
.CodeMirror-hints.custom .CodeMirror-hint{
	color: #FFDEAD;
}
.CodeMirror-hints.custom .CodeMirror-hint-active {
  background-color: #444 !important;
  color: #FFDEAD !important;
}
.cm-s-custom  { font-family: Consolas, Menlo, Monaco, Lucida Console, Liberation Mono, DejaVu Sans Mono, Bitstream Vera Sans Mono, Courier New, monospace, serif;}
.CodeMirror-scrollbar-filler, .CodeMirror-gutter-filler{
	background-color: #000;
}