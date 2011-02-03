$(document).ready(function(){

    if (!$.browser.webkit) {
        if ( ! ('placeholder' in $('<input/>')[0]) )
            $('input[placeholder]').placeholder();
     }
	
});
	
(function($){

	var placeholder_color='#BBB';
	
	$.fn.placeholder = function( color ) {
		
		if ( color )
			placeholder_color = color;
		else
			color = placeholder_color;
		
		$(this).each( function(i){
		
			if ( $(this).attr('type') == 'password' ) {

				var
					$field = $(this)
						.focusout(function(e){
							if ( $field.val() == '' )
								$([ $field[0], $dummy[0] ]).toggle();
                            $dummy[0].focus();
						})
						.toggle(),

					$dummy = $('<input/>', { type: 'text' })
						.focusin(function(e){
							$([ $field[0], $dummy[0] ]).toggle();
							$field.focus();
						});

					if ( ( 'class' in $field[0] ) )
						$dummy.attr('class', $field.attr('class'));

					if ( ( 'style' in $field[0] ) )
						$dummy.attr('style', $field.attr('style'));
                

					$dummy
						.val( $field.attr('placeholder') )
						.css('color', color)
						.toggle()
						.insertAfter( $field );
			}
			else {

				var orig = $(this).css('color');

				$(this)

					.val( $(this).attr('placeholder') )
					.css( 'color', color )

					.focusin(function( e ){
						if ( $(this).val() == $(this).attr('placeholder') ) {
							$(this).val('');
							$(this).css( 'color', orig );
						} })

					.focusout(function( e ){
						if ( $(this).val() == '' ) {
							$(this).val( $(this).attr('placeholder') );
							$(this).css( 'color', color );
					}
            });
			}
			
		} );
		
	}
	
})(jQuery);

