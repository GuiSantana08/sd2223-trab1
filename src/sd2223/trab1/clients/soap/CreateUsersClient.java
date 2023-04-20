package sd2223.trab1.clients.soap;

import sd2223.trab1.api.User;

public class CreateUsersClient {

        public static void main(String[] args) {
            if( args.length != 5) {
                System.err.println("usage: serverUri name pwd domain displayName");
                System.exit(0);
            }

            var serverURI = args[0];
            var name = args[1];
            var pwd = args[2];
            var domain = args[3];
            var displayName = args[4];

            var users = new SoapUsersClient( java.net.URI.create( serverURI ));

            var res = users.createUser( new User( name, pwd, domain, displayName) );
        }
}
