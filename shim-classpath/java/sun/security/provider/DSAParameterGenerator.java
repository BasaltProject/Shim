/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

import java.math.BigInteger;
import java.security.AlgorithmParameterGeneratorSpi;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAGenParameterSpec;
import java.security.spec.DSAParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.HashMap;
import java.util.Map;

public final class DSAParameterGenerator extends AlgorithmParameterGeneratorSpi {
  private static final Map<Integer, DSAParameterSpec> CACHE = new HashMap<>();

  private static final String[][] STANDARD = {
      {"512", "fca682ce8e12caba26efccf7110e526db078b05edecbcd1eb4a208f3ae1617ae01f35b91a47e6df63413c5e12ed0899bcd132acd50d99151bdc43ee737592e17", "962eddcc369cba8ebb260ee6b6a126d9346e38c5", "678471b27a9cf44ee91a49c5147db1a9aaf244f05a434d6486931d2d14271b9e35030b71fd73da179069b32e2935630e1c2062354d0da20a6c416e50be794ca4"},
      {"768", "e9e642599d355f37c97ffd3567120b8e25c9cd43e927b3a9670fbec5d890141922d2c3b3ad2480093799869d1e846aab49fab0ad26d2ce6a22219d470bce7d777d4a21fbe9c270b57f607002f3cef8393694cf45ee3688c11a8c56ab127a3daf", "9cdbd84c9f1ac2f38d0f80f42ab952e7338bf511", "30470ad5a005fb14ce2d9dcd87e38bc7d1b1c5facbaecbe95f190aa7a31d23c4dbbcbe06174544401a5b2c020965d8c2bd2171d3668445771f74ba084d2029d83c1c158547f3a9f1a2715be23d51ae4d3e5a1f6a7064f316933a346d3f529252"},
      {"1024", "fd7f53811d75122952df4a9c2eece4e7f611b7523cef4400c31e3f80b6512669455d402251fb593d8d58fabfc5f5ba30f6cb9b556cd7813b801d346ff26660b76b9950a5a49f9fe8047b1022c24fbba9d7feb7c61bf83b57e7c6a8a6150f04fb83f6d3c51ec3023554135a169132f675f3ae2b61d72aeff22203199dd14801c7", "9760508f15230bccb292b982a2eb840bf0581cf5", "f7e1a085d69b3ddecbbcab5c36b857b97994afbbfa3aea82f9574c0b3d0782675159578ebad4594fe67107108180b449167123e84c281613b7cf09328cc8a6e13c167a8b547c8d28e0a3ae1e2bb3a675916ea37f0bfa213562f1fb627a01243bcca4f1bea8519089a883dfe15ae59f06928b665e807b552564014c3bfecf492a"},
      {"2048", "8f7935d9b9aae9bfabed887acf4951b6f32ec59e3baf3718e8eac4961f3efd3606e74351a9c4183339b809e7c2ae1c539ba7475b85d011adb8b47987754984695cac0e8f14b3360828a22ffa27110a3d62a993453409a0fe696c4658f84bdd20819c3709a01057b195adcd00233dba5484b6291f9d648ef883448677979cec04b434a6ac2e75e9985de23db0292fc1118c9ffa9d8181e7338db792b730d7b9e349592f68099872153915ea3d6b8b4653c633458f803b32a4c2e0f27290256e4e3f8a3b0838a1c450e4e18c1a29a37ddf5ea143de4b66ff04903ed5cf1623e158d487c608e97f211cd81dca23cb6e380765f822e342be484c05763939601cd667", "baf696a68578f7dfdee7fa67c977c785ef32b233bae580c0bcd5695d", "16a65c58204850704e7502a39757040d34da3a3478c154d4e4a5c02d242ee04f96e61e4bd0904abdac8f37eeb1e09f3182d23c9043cb642f88004160edf9ca09b32076a79c32a627f2473e91879ba2c4e744bd2081544cb55b802c368d1fa83ed489e94e0fa0688e32428a5c78c478c68d0527b71c9a3abb0b0be12c44689639e7d3ce74db101a65aa2b87f64c6826db3ec72f4b5599834bb4edb02f7c90e9a496d3a55d535bebfc45d4f619f63f3dedbb873925c2f224e07731296da887ec1e4748f87efb5fdeb75484316b2232dee553ddaf02112b0d1f02da30973224fe27aeda8b9d4b2922d9ba8be39ed9e103a63c52810bc688b7e2ed4316e1ef17dbde"},
      {"3072", "ea9cda9f5fbda66dd830494609405687ab7cf38538e058d1e2f68dea95364866e1c05beacded24227edee28cad80bcecad39913be3b713267b3b96c8d9f0f6a03b5dfc9222d5cfe4afcc9982f33784f760c3b759aebe3bbe9098a6b84c96f1fde44ce11c084c2a082c7a76a0ef142928b4f328406ab9beb24f84577dd0f46ce86fd8f08488269998bf4742d6425f7a0ec75d8660c5dd6f4e3b3d3bee81b2c21afe8c9e8b84b87192e2cc20f961d2bcd8133afcf3675ab80681cb374c78f33e29d1011083d89f9c5728b94676fccb1b57bc60288c15d85ae838ae1941c5a20ae2b2049b3583fe30da455ddb3e6ad9b9955cd9bb5681431622beb0f92da533fcab496cebc447aa1bb5a8039522f2da98ff416289323a64df626ab6881870927dcee387f13b5c9d24d6cba1d82ed375a082506ee87bc7ae30067f4a94e2ee363d992c40f2725b5db4b3525ebde22bbbfd0fa124a588b0f5a4acb3a86951aff09f8c8198fb5b53da0c931cedc598b4f835b779d04d99026c7ba08c4b27f118ac1e3d", "c4eeac2bbab79bd831946d717a56a6e687547aa8e9c5494a5a4b2f4ca13d6c11", "42e5fa7844f8fa9d8998d830d004e7b15b1d276bcbe5f12c35ec90c1a25f5832018a6724bd9cdbe803b675509bed167f3d7cf8599fc865c6d5a0f79158c1bc918f00a944d0ad0f38f520fb91d85d82674d0d5f874faa5fcdfe56cd178c1afdc7ce8795727b7dee966ed0b3c5cedcef8aca628befebf2d105c7aff8eb0da9c9610737dd64dce1237b82c1b2bc8608d55ffda98d7189444e65883315669c05716bde36c78b130aa3df2e4d609914c7c8dc470f4e300187c775f81e7b1a9c0dce405d6eab2cbb9d9c4ef44412ba573dd403c4ed7bc2364772f56a30c48de78f5003f9371c55262d2c8ac2246ade3b02fdcfcf5cbfde74fbcbfe6e0e0fdf3160764f84d311c179a40af679a8f47ab13c8f706893245eb11edcce451fa2ab980019987f125d8dc96622d419ba0d71f16c6024dce9d364c3b26d8ec1a3c828f6c9d14b1d0333b95db77bfdbe3c6bce5337a1a5a7ace10111219448447197e2a344cc423be768bb89e27be6cbd22085614a5a3360be23b1bfbb6e6e6471363d32c85d31"},
  };

  private int size = 2048;
  private SecureRandom random;

  public DSAParameterGenerator() {
  }

  static int subprimeBits(int primeBits) {
    return primeBits <= 1024 ? 160 : primeBits <= 2048 ? 224 : 256;
  }

  static synchronized DSAParameterSpec generate(int primeBits, SecureRandom rnd) {
    DSAParameterSpec c = CACHE.get(primeBits);
    if (c != null) {
      return c;
    }
    for (String[] row : STANDARD) {
      if (Integer.parseInt(row[0]) == primeBits) {
        DSAParameterSpec std = new DSAParameterSpec(new BigInteger(row[1], 16), new BigInteger(row[2], 16),
            new BigInteger(row[3], 16));
        CACHE.put(primeBits, std);
        return std;
      }
    }
    int qBits = subprimeBits(primeBits);
    BigInteger q = BigInteger.probablePrime(qBits, rnd);
    BigInteger twoQ = q.shiftLeft(1);
    BigInteger p;
    while (true) {
      BigInteger x = new BigInteger(primeBits, rnd).setBit(primeBits - 1);
      BigInteger rem = x.mod(twoQ);
      p = x.subtract(rem).add(BigInteger.ONE);
      if (p.bitLength() == primeBits && p.isProbablePrime(40)) {
        break;
      }
    }
    BigInteger e = p.subtract(BigInteger.ONE).divide(q);
    BigInteger g;
    BigInteger h = BigInteger.TWO;
    do {
      g = h.modPow(e, p);
      h = h.add(BigInteger.ONE);
    } while (g.compareTo(BigInteger.ONE) <= 0);
    DSAParameterSpec spec = new DSAParameterSpec(p, q, g);
    CACHE.put(primeBits, spec);
    return spec;
  }

  @Override
  protected void engineInit(int size, SecureRandom random) {
    this.size = size;
    this.random = random;
  }

  @Override
  protected void engineInit(AlgorithmParameterSpec genParamSpec, SecureRandom random)
      throws InvalidAlgorithmParameterException {
    if (genParamSpec instanceof DSAGenParameterSpec) {
      this.size = ((DSAGenParameterSpec) genParamSpec).getPrimePLength();
    } else {
      throw new InvalidAlgorithmParameterException("Invalid parameter");
    }
    this.random = random;
  }

  @Override
  protected AlgorithmParameters engineGenerateParameters() {
    try {
      DSAParameterSpec spec = generate(size, random != null ? random : new SecureRandom());
      AlgorithmParameters ap = AlgorithmParameters.getInstance("DSA");
      ap.init(spec);
      return ap;
    } catch (NoSuchAlgorithmException | InvalidParameterSpecException e) {
      throw new RuntimeException(e);
    }
  }
}
