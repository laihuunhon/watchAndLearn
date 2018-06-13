var users = [
    { 
      id: '4de37e03aa4b4372843a7eb33fa41cad', 
      nickname: "JimBig", 
      password: '12345', 
      name: 'Jim Smith',
      profile_picture: "http://i.blurr.com/profiles/4de37e03aa4b4372843a7eb33fa41cad.jpeg"
    }
];

exports.findByUsername = function(nickname) {
  for (var i = 0, len = users.length; i < len; i++) {
    var user = users[i];
    if (user.nickname === nickname) {
      return user;
    }
  }
  return null;
};


exports.findByID = function(id) {
  for (var i = 0, len = users.length; i < len; i++) {
    var user = users[i];
    if (user.id === id) {
      return user;
    }
  }
  return null;
};
