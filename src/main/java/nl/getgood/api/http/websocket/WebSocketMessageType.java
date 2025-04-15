package nl.getgood.api.http.websocket;

/**
 * Created on 05/08/2024 at 11:11
 * by Luca Warmenhoven.
 */
public enum WebSocketMessageType
{
    CONTINUING((byte) 0x0),
    TEXT((byte) 0x1),
    BINARY((byte) 0x2),
    CLOSE( (byte) 0x8),
    PING((byte) 0x9),
    PONG((byte) 0xA);

    final byte opcode;
    WebSocketMessageType(final byte opcode) {
        this.opcode = opcode;
    }

    /**
     * Method for decoding the opcode of a message.
     * @param opcode The opcode to decode.
     * @return The message type.
     */
    public static WebSocketMessageType decode(byte opcode) {
        return WebSocketMessageType.values()[opcode & 0xF];
    }
    // Returns the opcode of this message type.
    public byte getOpcode() { return this.opcode; }


}
