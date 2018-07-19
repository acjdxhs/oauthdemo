use oauthdemo;

create table oauthclient(
  `clientId` varchar(100) primary key ,
  `clientSecret` varchar(100)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into oauthclient(clientId, clientSecret) values ("infosec client", "infosec secret");

create table user (
  `username` varchar (100) primary key ,
  `password` varchar (100) ,
  `message` varchar (100)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table oauthinfo(
  `clientId` varchar (100),
  `redirectUrl` varchar (100),
  `username` varchar (100),
  `code` char (16),
  primary key (clientId, redirectUrl, username)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table token(
  `username` varchar (100),
  `token` varchar (100),
  primary key (username, token)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;