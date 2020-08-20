package io.openschema.mma.register;

/**
 * Client or UE need to be registered on the Cloud with their UUID and Key for the bootstrapping process.
 * Registration is manual now but should be automated.
 *
 * Sample Service
 * message UeParams {
 * 	bytes key = 1;
 * 	string uuid = 2;
 * }
 * service RegisterUE {
 *   rpc Register (UeParams) returns (Response) {}
 * }
 * message Response {
 *   string response = 1;
 * }
 */
public class RegisterationManager {
}
