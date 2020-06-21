NoOutboundConnectionsInputPort = draw2d.InputPort.extend({
    NAME: 'NoOutboundConnectionsInputPort',

    init: function(attr, setter, getter) {
        this._super(attr, setter, getter);
    },

    createCommand: function(request) {
       return null;
    }
});