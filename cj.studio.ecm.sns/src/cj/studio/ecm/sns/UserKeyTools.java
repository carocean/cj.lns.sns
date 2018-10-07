package cj.studio.ecm.sns;

import java.util.HashMap;

import cj.ultimate.security.Base64Utils;
import cj.ultimate.security.RSAUtils;

public class UserKeyTools {
	private String user;
	private String privateKey;
	private String publicKey;
	
	public UserKeyTools(String user, String privateKey, String publicKey) {
		super();
		this.user = user;
		this.privateKey = privateKey;
		this.publicKey = publicKey;
	}
	public boolean verifySign(String cjtoken) throws Exception {
		byte[] encodedData=Base64Utils.decode(cjtoken);
		String sign = RSAUtils.sign(encodedData, privateKey);
        boolean status = RSAUtils.verify(encodedData, publicKey, sign);
		return status;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPrivateKey() {
		return privateKey;
	}
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	public void fill(HashMap<String, String> map) {
		user=map.get("user");
		privateKey=map.get("privateKey");
		publicKey=map.get("publicKey");
	}
	public String newToken(byte[] encodedData) throws Exception {
		return RSAUtils.sign(encodedData, privateKey);
	}

}
