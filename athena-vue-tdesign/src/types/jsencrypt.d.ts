declare module 'jsencrypt' {
  export default class JSEncrypt {
    setPublicKey(key: string): void;
    setPrivateKey(key: string): void;
    encrypt(plain: string): string | false;
    decrypt(cipher: string): string | false;
  }
}
