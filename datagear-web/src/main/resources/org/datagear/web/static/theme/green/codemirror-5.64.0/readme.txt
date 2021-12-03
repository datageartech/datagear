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
    color: #e3e3e3 !important;
    -webkit-box-shadow: 0px 0px 6px #cccccc;
    box-shadow: 0px 0px 6px #cccccc;
}
.CodeMirror-hints.custom .CodeMirror-hint{
	color: #e3e3e3;
}
.CodeMirror-hints.custom .CodeMirror-hint-active {
  background-color: #459e05 !important;
  color: #e3e3e3 !important;
}