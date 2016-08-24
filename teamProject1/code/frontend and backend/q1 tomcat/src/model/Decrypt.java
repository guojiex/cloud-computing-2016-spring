package model;

import java.math.BigInteger;

public class Decrypt {
	private final static BigInteger X = new BigInteger(
			"64266330917908644872330635228106713310880186591609208114244758680898150367880703152525200743234420230");

	public static String getDecrypt(String keyY, String message) {
		BigInteger Y = new BigInteger(keyY);
		BigInteger Z = X.gcd(Y);
		int k = 1 + Z.mod(BigInteger.valueOf(25)).intValue();
		int length = (int) Math.sqrt(message.length());
		return decrypt(message, length, k);
	}

	private static String decrypt(String message, int length, int k) {
		StringBuilder sb = new StringBuilder();
		char[][] charMatrix = new char[length][length];
		boolean[][] vistited = new boolean[length][length];
		for (int i = 0, index = 0; i < length; i++) {
			for (int j = 0; j < length; j++) {
				charMatrix[i][j] = message.charAt(index);
				index++;
			}
		}
		int mode = 0;
		int i = 0, j = 0;
		int count = 0;
		while (count != length * length) {
			vistited[i][j] = true;
			count++;
			int temp=charMatrix[i][j] - k;
			if(temp<'A')
				temp+=26;
			sb.append((char)temp);
			switch (mode) {
			case 0:
				if (j + 1 < length && vistited[i][j + 1] == false)
					++j;
				else {
					mode = 1;
					++i;
				}
				break;
			case 1:
				if (i + 1 < length && vistited[i + 1][j] == false)
					++i;
				else {
					mode = 2;
					--j;
				}
				break;
			case 2:
				if (j - 1 >= 0 && vistited[i][j - 1] == false)
					--j;
				else {
					mode = 3;
					--i;
				}
				break;
			case 3:
				if (i - 1 >= 0 && vistited[i - 1][j] == false)
					--i;
				else {
					mode = 0;
					++j;
				}
				break;
			}
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out
				.println(getDecrypt(
						"4024123659485622445001958636275419709073611535463684596712464059093821",
						"URYEXYBJB"));
	}

}
