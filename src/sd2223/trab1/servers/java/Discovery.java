package sd2223.trab1.servers.java;

import com.fasterxml.jackson.core.JsonToken;

import java.net.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * <p>
 * A class interface to perform service discovery based on periodic
 * announcements over multicast communication.
 * </p>
 * 
 */

public interface Discovery {

	/**
	 * Used to announce the URI of the given service name.
	 *
	 * @param serviceName - the name of the service
	 * @param serviceURI  - the uri of the service
	 */
	 void announce(String serviceName, String serviceURI);

	 URI uriServDom(String dom, String serv);

	/**
	 * Get discovered URIs for a given service name
	 * 
	 * @param serviceName - name of the service
	 * @param minReplies  - minimum number of requested URIs. Blocks until the
	 *                    number is satisfied.
	 * @return array with the discovered URIs for the given service name.
	 */
	URI[] knownUrisOf(String serviceName, int minReplies);

	/**
	 * Get the instance of the Discovery service
	 * 
	 * @return the singleton instance of the Discovery service
	 */
	 static Discovery getInstance() {
		return DiscoveryImpl.getInstance();
	}
}

/**
 * Implementation of the multicast discovery service
 */
class DiscoveryImpl implements Discovery {

	private Map<String, List<URI>> listURIs = new HashMap<>();

	private Map<String, Map<String, URI>> servDomURIS = new HashMap<>();

	private static Logger Log = Logger.getLogger(Discovery.class.getName());

	// The pre-aggreed multicast endpoint assigned to perform discovery.

	static final int DISCOVERY_RETRY_TIMEOUT = 5000;
	static final int DISCOVERY_ANNOUNCE_PERIOD = 1000;

	// Replace with appropriate values...
	static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("226.226.226.226", 2266);

	// Used separate the two fields that make up a service announcement.
	private static final String DELIMITER = "\t";

	private static final int MAX_DATAGRAM_SIZE = 65536;

	private static Discovery singleton;


	synchronized static Discovery getInstance() {
		if (singleton == null) {
			singleton = new DiscoveryImpl();
		}
		return singleton;
	}

	private DiscoveryImpl() {
		this.startListener();
	}

	@Override
	public void announce(String serviceName, String serviceURI) {
		Log.info(String.format("Starting Discovery announcements on: %s for: %s -> %s\n", DISCOVERY_ADDR, serviceName,
				serviceURI));

		var pktBytes = String.format("%s%s%s", serviceName, DELIMITER, serviceURI).getBytes();
		var pkt = new DatagramPacket(pktBytes, pktBytes.length, DISCOVERY_ADDR);

		// start thread to send periodic announcements
		new Thread(() -> {
			try (var ds = new DatagramSocket()) {
				while (true) {
					try {
						ds.send(pkt);
						Thread.sleep(DISCOVERY_ANNOUNCE_PERIOD);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

	@Override
	public URI[] knownUrisOf(String serviceName, int minEntries) {
		List<URI> URIs = listURIs.get(serviceName);
		URI[] uri = new URI[URIs.size()];
		int counter = 0;
		synchronized (listURIs) {
			Iterator it = URIs.iterator();
			while (it.hasNext())
				uri[counter++] = (URI) it.next();
		}
		return uri;
	}

	public URI uriServDom(String dom, String serv){
		System.out.println("PRINTTTTT DOS DOOMMMMMMMMMMMSSMMMMSSMMM");
		System.out.println(servDomURIS.get(dom));
		return servDomURIS.get(dom).get(serv);
	}

	private void startListener() {
		Log.info(String.format("Starting discovery on multicast group: %s, port: %d\n", DISCOVERY_ADDR.getAddress(),
				DISCOVERY_ADDR.getPort()));

		new Thread(() -> {
			try (var ms = new MulticastSocket(DISCOVERY_ADDR.getPort())) {
				ms.joinGroup(DISCOVERY_ADDR, NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
				for (;;) {
					try {
						var pkt = new DatagramPacket(new byte[MAX_DATAGRAM_SIZE], MAX_DATAGRAM_SIZE);
						ms.receive(pkt);

						var msg = new String(pkt.getData(), 0, pkt.getLength());
						Log.info(String.format("Received in DISCOVERY: %s", msg));

						var parts = msg.split(DELIMITER);
						if (parts.length == 2) {
							// TODO: complete by storing the decoded announcements...
							String serviceName = parts[0];
							var uri = URI.create(parts[1]);
							var list = listURIs.get(serviceName);
							synchronized (listURIs) {
								if (list != null) {
									list.add(uri);
								}
								else {
									list = new ArrayList<>();
									list.add(uri);
									listURIs.put(serviceName, list);
								}
							}
						}
					} catch (Exception x) {
						x.printStackTrace();
					}
				}
			} catch (Exception x) {
				x.printStackTrace();
			}
		}).start();
	}
}