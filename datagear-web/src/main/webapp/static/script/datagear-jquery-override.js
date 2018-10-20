/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 重写jquery、jquery-ui函数。
 * 
 * 依赖:
 * jquery.js
 * jquery-ui.js
 * datagear-util.js
 */

(function($, undefined)
{
	//*************重写jQuery.param函数******************
	//jQuery.param对于对象属性，也会序列化为“obj[property-name]”的参数名，不符合后台处理习惯
	//这里重写此方法：对于“obj[property-name]”，将被序列化为“obj.property-name”的参数名
	
	var r20 = /%20/g,
	rbracket = /\[\]$/,
	rCRLF = /\r?\n/g,
	rsubmitterTypes = /^(?:submit|button|image|reset|file)$/i,
	rsubmittable = /^(?:input|select|textarea|keygen)/i;

	function buildParams( prefix, obj, traditional, add ) {
		var name;
	
		if ( jQuery.isArray( obj ) ) {
	
			// Serialize array item.
			jQuery.each( obj, function( i, v ) {
				if ( traditional || rbracket.test( prefix ) ) {
	
					// Treat each array item as a scalar.
					add( prefix, v );
	
				} else {
	
					// Item is non-scalar (array or object), encode its numeric index.
					buildParams(
						//XXX 原内容（“///”移除后）
						///prefix + "[" + ( typeof v === "object" && v != null ? i : "" ) + "]",
						//XXX 原内容
						//XXX 替换内容
						//将param[name]格式的参数名修改为param.name格式
						prefix + "[" + i + "]",
						//XXX 替换内容
						v,
						traditional,
						add
					);
				}
			} );
	
		} else if ( !traditional && jQuery.type( obj ) === "object" ) {
	
			// Serialize object item.
			for ( name in obj ) {
				
				//XXX 原内容（“///”移除后）
				///buildParams( prefix + "[" + name + "]", obj[ name ], traditional, add );
				//XXX 原内容
				//XXX 替换内容
				//将param[name]格式的参数名修改为param.name格式
				buildParams( prefix + "." + name, obj[ name ], traditional, add );
				//XXX 替换内容
			}
	
		} else {
	
			// Serialize scalar item.
			add( prefix, obj );
		}
	}
	
	// Serialize an array of form elements or a set of
	// key/values into a query string
	$.param = function( a, traditional ) {
		
		//XXX 新增内容
		//处理重复引用
		a = $.refParam(a);
		//XXX 新增内容
		
		var prefix,
			s = [],
			add = function( key, value ) {
	
				//XXX 原内容（“///”移除后）
				// If value is a function, invoke it and return its value
				///value = jQuery.isFunction( value ) ? value() : ( value == null ? "" : value );
				///s[ s.length ] = encodeURIComponent( key ) + "=" + encodeURIComponent( value );
				//XXX 原内容
				//XXX 替换内容
				//对于null值，不传输到后台，以免复合null属性值类型转换报错
				value = jQuery.isFunction( value ) ? value() : value;
				if(value != null)
					s[ s.length ] = encodeURIComponent( key ) + "=" + encodeURIComponent( value );
				//XXX 替换内容
			};
	
		// Set traditional to true for jQuery <= 1.3.2 behavior.
		if ( traditional === undefined ) {
			traditional = jQuery.ajaxSettings && jQuery.ajaxSettings.traditional;
		}
	
		// If an array was passed in, assume that it is an array of form elements.
		if ( jQuery.isArray( a ) || ( a.jquery && !jQuery.isPlainObject( a ) ) ) {
	
			// Serialize the form elements
			jQuery.each( a, function() {
				add( this.name, this.value );
			} );
	
		} else {
	
			// If traditional, encode the "old" way (the way 1.3.2 or older
			// did it), otherwise encode params recursively.
			for ( prefix in a ) {
				buildParams( prefix, a[ prefix ], traditional, add );
			}
		}
	
		// Return the resulting serialization
		return s.join( "&" ).replace( r20, "+" );
	};
	//*************重写jQuery.param函数******************
	
	//*************重写jQuery-ui的$.widget.extend函数******************
	//$.widget.extend会深度拷贝对象，包括组件的options，而options可能会有循环引用，这会导致$.widget.extend溢出
	//这里重写此方法：只要对象中包含了引用，那么就不做深度拷贝，这样做影响最小，因为只可能时应用数据才会出现引用
	
	var widgetSlice = Array.prototype.slice;
	
	//XXX 原内容（“///”移除后）
	///$.widget.extend = function( target ) {
	//XXX 原内容
	//XXX 替换内容
	//isNotRefFlag用于标识对象内部是否存在引用，这样可以减少$.isRef的调用次数，提升性能
	$.widget.extend = function( target, isNotRefFlag ) {
	//XXX 替换内容
		var input = widgetSlice.call( arguments, 1 );
		var inputIndex = 0;
		var inputLength = input.length;
		var key;
		var value;
		
		//XXX 新增内容
		var isNotRef = (input[inputLength - 1] == "___not_ref_flag___");
		if(isNotRef)
			inputLength = inputLength - 1;
		//XXX 新增内容

		for ( ; inputIndex < inputLength; inputIndex++ ) {
			for ( key in input[ inputIndex ] ) {
				value = input[ inputIndex ][ key ];
				if ( input[ inputIndex ].hasOwnProperty( key ) && value !== undefined ) {

					// Clone objects
					//XXX 原内容（“///”移除后）
					///if ( $.isPlainObject( value ) ) {
					//XXX 原内容
					//XXX 替换内容
					//只有不存在引用的才执行深度拷贝
					if ( $.isPlainObject( value ) && (isNotRef || !$.isRef(value)) ) {
					//XXX 替换内容
						target[ key ] = $.isPlainObject( target[ key ] ) ?
							//XXX 原内容（“///”移除后）
							///$.widget.extend( {}, target[ key ], value ) :
							//XXX 原内容
							//XXX 替换内容
							$.widget.extend( {}, target[ key ], value, "___not_ref_flag___" ) :
							//XXX 替换内容

							// Don't extend strings, arrays, etc. with objects
							//XXX 原内容（“///”移除后）
							///$.widget.extend( {}, value );
							//XXX 原内容
							//XXX 替换内容
							$.widget.extend( {}, value, "___not_ref_flag___" );
							//XXX 替换内容

					// Copy everything else by reference
					} else {
						target[ key ] = value;
					}
				}
			}
		}
		return target;
	};
	//*************重写jQuery-ui的$.widget.extend函数******************
})
(jQuery);