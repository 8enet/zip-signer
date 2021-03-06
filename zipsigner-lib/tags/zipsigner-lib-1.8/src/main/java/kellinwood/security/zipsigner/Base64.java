/*
 * Copyright (C) 2010 Ken Ellinwood.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package kellinwood.security.zipsigner;

import org.bouncycastle.util.encoders.Base64Encoder;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

public class Base64 {

    static Base64Encoder encoder = new Base64Encoder();

    public static String encode( byte[] data) {

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            encoder.encode( data, 0, data.length, baos);
            return new String( baos.toByteArray());
        }
        catch (IOException x) {
            throw new IllegalStateException( x.getClass().getName() + ": " + x.getMessage());
        }
    }
}