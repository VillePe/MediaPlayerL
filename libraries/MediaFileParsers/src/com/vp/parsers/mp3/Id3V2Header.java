/*
 * 
 * 
 * 
 */

package com.vp.parsers.mp3;

public class Id3V2Header {
        private String fileIdentifier;
        private int versionMajor;
        private int versionMinor;
        private boolean[] flags = new boolean[8];
        private long size;
        
        public Id3V2Header() {
            
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("File identifier: ").append(fileIdentifier).append("\n");
            sb.append("Version: ").append(versionMajor).append(".").append(versionMinor).append("\n");
            for (int i = 0; i < flags.length; i++) {
                sb.append("Flag ").append(i).append(". :").append(flags[i]).append("\n");
            }
            sb.append("Size: ").append(size).append("\n");
            return sb.toString();
        }
        
        // Parses the header according the ID3 standards
        // For more info see http://id3.org/id3v2.3.0
        public boolean parseHeader(int[] bytes) {
            if (bytes.length != 10) {
                return false;
            }
            if (!parseIdentifier(bytes)) {
                return false;
            }            
            parseVersion(bytes);
            parseFlags(bytes);
            parseSize(bytes);
            
            return true;
        }
        
        private boolean parseIdentifier(int[] bytes) {
            if (    bytes[0] == 'I' && 
                    bytes[1] == 'D' && 
                    bytes[2] == '3') {
                fileIdentifier = "ID3";
                return true;
            }
            return false;
        }
        
        private boolean parseVersion(int[] bytes) {
            if (bytes[3] == 0xFF || bytes[4] == 0xFF) {
                return false;
            } else {
                versionMajor = bytes[3];
                versionMinor = bytes[4];
                return true;
            }
        }
        
        private void parseFlags(int[] bytes) {
            int mask7 = 0b10000000;
            int mask6 = 0b01000000;
            int mask5 = 0b00100000;
            int mask4 = 0b00010000;
            if ((bytes[5] & mask7) >> 7 == 1) {
                flags[0] = true; 
            }
            if ((bytes[5] & mask6) >> 6 == 1) {
                flags[1] = true;
            }
            if ((bytes[5] & mask5) >> 5 == 1) {
                flags[2] = true;
            }
            if ((bytes[5] & mask4) >> 4 == 1) {
                flags[3] = true; 
            }
        }
        
        private void parseSize(int[] bytes) {
            // All bytes have an extra zero in front that needs to be taken away
            //  0010 0011 0001 0111 0110 1000 0101 0101     // Original
            //   010 0011  001 0111  110 1000  101 0101     // Without zeros
            //       0100 0110 0101 1111 0100 0101 0101     // The result
            size = (bytes[6]  << 21) | (bytes[7] << 14) | (bytes[8] << 7) | (bytes[9]); 
        }

        public String getFileIdentifier() {
            return fileIdentifier;
        }

        public void setFileIdentifier(String fileIdentifier) {
            this.fileIdentifier = fileIdentifier;
        }

        public int getVersionMajor() {
            return versionMajor;
        }

        public void setVersionMajor(int versionMajor) {
            this.versionMajor = versionMajor;
        }

        public int getVersionMinor() {
            return versionMinor;
        }

        public void setVersionMinor(int versionMinor) {
            this.versionMinor = versionMinor;
        }

        public boolean[] getFlags() {
            return flags;
        }

        public void setFlags(boolean[] flags) {
            this.flags = flags;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }
        
        public enum Flags {
            UNSYNCHRO (0), EXT_HEADER (1), EXPERIMENTAL (2);
            private final int index;
            
            Flags(int index) {
                this.index = index;
            }
            public int getIndex() {
                return index;
            }
        }
        
    }
