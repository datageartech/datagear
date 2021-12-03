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
  color: #ffb496 !important;
  -webkit-box-shadow: 0px 0px 6px #cccccc;
  box-shadow: 0px 0px 6px #cccccc;
}
.CodeMirror-hints.custom .CodeMirror-hint{
	color: #ffb496;
}
.CodeMirror-hints.custom .CodeMirror-hint-active {
  background-color: #444 !important;
  color: #ffb496 !important;
}