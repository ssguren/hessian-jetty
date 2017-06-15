package hessian.server.hetty.core.ssl;

import hessian.conf.HettyConfig;

import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyPair;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Vector;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;

import org.apache.log4j.Logger;
import org.bouncycastle.openssl.PEMParser;

import base.util.FileUtil;
import base.util.MiscUtil;

public class PEMKeyManager extends X509ExtendedKeyManager {

	private static final Logger log = Logger.getLogger(PEMKeyManager.class);

	protected static PEMKeyManager instance = new PEMKeyManager();

	private PrivateKey key;

	private X509Certificate[] chain;

	private PEMKeyManager() {
		PEMParser reader = null;
		try {
			final HettyConfig hettyConfig = HettyConfig.getInstance();

			reader = new PEMParser(new FileReader(FileUtil.getFile(hettyConfig
					.getCertificateKeyFile())));
			key = ((KeyPair) reader.readObject()).getPrivate();

			reader = new PEMParser(new FileReader(FileUtil.getFile(hettyConfig
					.getCertificateFile())));

			Vector<X509Certificate> chainVector = new Vector<X509Certificate>();
			X509Certificate cert;
			while ((cert = (X509Certificate) reader.readObject()) != null) {
				chainVector.add(cert);
			}
			chain = (X509Certificate[]) chainVector
					.toArray(new X509Certificate[1]);
		} catch (Exception e) {
			log.fatal(MiscUtil.traceInfo(e));
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				log.error(MiscUtil.traceInfo(e));
			}
		}
	}

	public String chooseEngineServerAlias(String s, Principal[] principals,
			SSLEngine sslEngine) {
		return "";
	}

	public String[] getClientAliases(String s, Principal[] principals) {
		return new String[] { "" };
	}

	public String chooseClientAlias(String[] strings, Principal[] principals,
			Socket socket) {
		return "";
	}

	public String[] getServerAliases(String s, Principal[] principals) {
		return new String[] { "" };
	}

	public String chooseServerAlias(String s, Principal[] principals,
			Socket socket) {
		return "";
	}

	public X509Certificate[] getCertificateChain(String s) {
		return chain;
	}

	public PrivateKey getPrivateKey(String s) {
		return key;
	}
}
