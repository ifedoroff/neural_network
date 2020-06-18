var InputTier = BaseTier.extend({
    NAME: "InputTier",

    init:function(attr, setter, getter) {
        this._super($.extend({level: 0}, attr), setter, getter);
    },

    getLabelText: function() {
        return 'Input Tier';
    },

    createNeuron: function(position) {
        return new InputNode({position: position, level: this.level})
    },

    onContextMenu:function(x,y){
        $.contextMenu({
            selector: '.InputTier, .draw2d_shape_basic_Rectangle',
            events:
                {
                    hide:function(){ $.contextMenu( 'destroy' ); }
                },
            callback: function(key, options)
            {
                switch(key){
                    case "delete":
                        this.fireEvent('delete');
                        break;
                    case "addNeuron":
                        this.addNeuron();
                        break;
                    default:
                        break;
                }

            }.bind(this),
            x:x,
            y:y,
            items:
                {
                    "addNeuron":    {name: "Add Input Node"},
                    "delete":  {name: "Delete"}
                }
        });
    }
});