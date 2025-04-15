package nl.getgood.api.http.websocket;

import nl.getgood.api.Logger;

import java.nio.charset.StandardCharsets;

/**
 * Created on 05/08/2024 at 11:10
 * by Luca Warmenhoven.
 */
public class WebSocketFrame
{
    private final WebSocketMessageType type;
    private final String content;
    private final byte[] maskingKey;

    public WebSocketFrame( WebSocketMessageType type, String content )
    {
        this.type = type;
        this.content = content;
        this.maskingKey = null;
    }

    public WebSocketFrame( WebSocketMessageType type, String content, byte[] maskingKey )
    {
        this.type = type;
        this.content = content;
        this.maskingKey = maskingKey;
    }

    public WebSocketMessageType getType()
    {
        return type;
    }

    public String getContent()
    {
        return content;
    }

    public static WebSocketFrame decode( byte[] data )
    {
        // Check if the packet isn't corrupted
        if ( data.length <= 2 || ( data[0] & 0b00001110 ) != 0 )
            throw new IllegalArgumentException( "Attempted to decode corrupted WebSocket frame data" );

        WebSocketMessageType opcode = WebSocketMessageType.decode( ( byte ) ( data[0] & 0b00001111 ) );
        byte payloadLength = ( byte ) ( data[1] & 0b01111111 ); // Payload size (7 bits)
        boolean fin = ( data[0] & 0b10000000 ) != 0; // Whether this is the whole message
        boolean masked = ( data[1] & 0b10000000 ) != 0; // Whether the 9th bit is masked

        long extendedPayloadLength = 0;
        int byteOffset = 2;

        // Check whether the payload length is 16-bit
        if ( payloadLength == 126 )
        {
            // 16-bit extended payload length (byte 2 and 3)
            extendedPayloadLength = ( ( data[2] & 0xFF ) << 8 ) | ( data[3] & 0xFF );
            byteOffset += 2;

            // Check whether payload length is 64-bit
        }
        else if ( payloadLength == 127 )
        {
            // 64-bit extended payload length
            for ( int i = 0; i < 8; i++ )
                extendedPayloadLength |= ( long ) ( data[2 + i] & 0xFF ) << ( ( 7 - i ) * 8 );
            byteOffset += 8;

        }
        else
        {
            extendedPayloadLength = payloadLength;
        }

        Logger.getLogger().verbose( "[WebSocket] [Decoding] - Whole message: %b, payload length: %d, " +
                                            "extendedPayloadLength: %d, opcode: %s, masked: %b, data length: %d",
                                    fin, payloadLength, extendedPayloadLength, opcode, masked, data.length );

        byte[] maskingKey = null;
        byte[] payloadData = new byte[( int ) extendedPayloadLength];


        // Check if the MASK bit was set, if so, copy the key to the `maskingKey` array.
        if ( masked )
        {
            maskingKey = new byte[4];
            System.arraycopy( data, byteOffset, maskingKey, 0, 4 ); // move mask bytes
            byteOffset += 4;
        }

        // Copy payload bytes into `payloadData` array.
        System.arraycopy( data, byteOffset, payloadData, 0, payloadData.length );

        // If mask is present, decode the payload data with the mask.
        if ( masked )
            for ( int i = 0; i < payloadData.length; i++ )
                payloadData[i] ^= maskingKey[i % 4];

        if ( masked )
        {
            return new WebSocketFrame( opcode, new String( payloadData, StandardCharsets.UTF_8 ), maskingKey );
        }

        // Convert payload data to string
        return new WebSocketFrame( opcode, new String( payloadData, StandardCharsets.UTF_8 ) );

    }

    /**
     * Encodes this WebSocket frame with the given opcode.
     *
     * @param opcode The opcode of the frame
     * @return The encoded frame
     */
    public byte[] encode( WebSocketMessageType opcode )
    {
        return encode( opcode, this.content, true, this.maskingKey );
    }

    /**
     * Encodes a WebSocket frame with the given opcode and data.
     *
     * @param opcode The opcode of the frame
     * @param data   The data to encode
     * @return The encoded frame
     */
    public static byte[] encode( WebSocketMessageType opcode, String data )
    {
        return encode( opcode, data, true, null );
    }

    public static byte[] encode( WebSocketMessageType opcode, String data, boolean fin, byte[] maskingKey )
    {
        boolean masked = maskingKey != null;
        byte[] payloadData = data.getBytes(StandardCharsets.UTF_8);

        int payloadLength = payloadData.length;
        int extendedPayloadLength = payloadLength;
        int payloadLengthFieldSize = 1;

        if (payloadLength >= 126) {
            if (payloadLength < 65536) {
                extendedPayloadLength = 126;
                payloadLengthFieldSize = 2;
            } else {
                extendedPayloadLength = 127;
                payloadLengthFieldSize = 8;
            }
        }

        int frameLength = 2 + payloadLengthFieldSize + (masked ? 4 : 0) + payloadLength;
        byte[] frame = new byte[frameLength];

        // FIN + RSV1-3 + OPCODE
        frame[0] = (byte) ((fin ? 0x80 : 0x00) | opcode.getOpcode());
        // MASK + PAYLOAD LENGTH
        frame[1] = (byte) ((masked ? 0x80 : 0x00) | extendedPayloadLength);


        int index = 2;
        if (extendedPayloadLength == 126) {
            frame[index++] = (byte) (payloadLength >> 8);
            frame[index++] = (byte) (payloadLength);
        } else if (extendedPayloadLength == 127) {
            for (int i = 7; i >= 0; i--) {
                frame[index++] = (byte) (payloadLength >> (i * 8));
            }
        }

        if (masked) {
            System.arraycopy(maskingKey, 0, frame, index, 4);
            index += 4;
            for (int i = 0; i < payloadData.length; i++) {
                frame[index + i] = (byte) (payloadData[i] ^ maskingKey[i % 4]);
            }
        } else {
            System.arraycopy(payloadData, 0, frame, index, payloadData.length);
        }

        for ( byte b : frame )
        {
            for ( int i = 7; i >= 0; i-- )
                System.out.print( ( b >> i ) & 1 );
            System.out.print(" ");
        }
        System.out.println();

        return frame;
    }
}
