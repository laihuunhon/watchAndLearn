var clients = [
    { clientId: 'ADfgIAIgrCBU25XYY5G', clientSecret: 'mtdOl0EGu0fVtpCMrVQi06pxpb69O0' }
];

exports.findByClientId = function(clientId) {
  for (var i = 0, len = clients.length; i < len; i++) {
    var client = clients[i];
    if (client.clientId === clientId) {
      return client;
    }
  }
  return null;
};
