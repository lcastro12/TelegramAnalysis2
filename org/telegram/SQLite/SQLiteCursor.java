package org.telegram.SQLite;

import java.nio.ByteBuffer;
import org.telegram.messenger.FileLog;
import org.telegram.tgnet.NativeByteBuffer;

public class SQLiteCursor {
    public static final int FIELD_TYPE_BYTEARRAY = 4;
    public static final int FIELD_TYPE_FLOAT = 2;
    public static final int FIELD_TYPE_INT = 1;
    public static final int FIELD_TYPE_NULL = 5;
    public static final int FIELD_TYPE_STRING = 3;
    boolean inRow = false;
    SQLitePreparedStatement preparedStatement;

    native int columnByteArrayLength(int i, int i2);

    native byte[] columnByteArrayValue(int i, int i2);

    native int columnByteBufferValue(int i, int i2, ByteBuffer byteBuffer);

    native double columnDoubleValue(int i, int i2);

    native int columnIntValue(int i, int i2);

    native int columnIsNull(int i, int i2);

    native long columnLongValue(int i, int i2);

    native String columnStringValue(int i, int i2);

    native int columnType(int i, int i2);

    public SQLiteCursor(SQLitePreparedStatement stmt) {
        this.preparedStatement = stmt;
    }

    public boolean isNull(int columnIndex) throws SQLiteException {
        checkRow();
        if (columnIsNull(this.preparedStatement.getStatementHandle(), columnIndex) == 1) {
            return true;
        }
        return false;
    }

    public int intValue(int columnIndex) throws SQLiteException {
        checkRow();
        return columnIntValue(this.preparedStatement.getStatementHandle(), columnIndex);
    }

    public double doubleValue(int columnIndex) throws SQLiteException {
        checkRow();
        return columnDoubleValue(this.preparedStatement.getStatementHandle(), columnIndex);
    }

    public long longValue(int columnIndex) throws SQLiteException {
        checkRow();
        return columnLongValue(this.preparedStatement.getStatementHandle(), columnIndex);
    }

    public String stringValue(int columnIndex) throws SQLiteException {
        checkRow();
        return columnStringValue(this.preparedStatement.getStatementHandle(), columnIndex);
    }

    public byte[] byteArrayValue(int columnIndex) throws SQLiteException {
        checkRow();
        return columnByteArrayValue(this.preparedStatement.getStatementHandle(), columnIndex);
    }

    public int byteArrayLength(int columnIndex) throws SQLiteException {
        checkRow();
        return columnByteArrayLength(this.preparedStatement.getStatementHandle(), columnIndex);
    }

    public int byteBufferValue(int columnIndex, ByteBuffer buffer) throws SQLiteException {
        checkRow();
        return columnByteBufferValue(this.preparedStatement.getStatementHandle(), columnIndex, buffer);
    }

    public int byteBufferValue(int columnIndex, NativeByteBuffer buffer) throws SQLiteException {
        checkRow();
        return columnByteBufferValue(this.preparedStatement.getStatementHandle(), columnIndex, buffer.buffer);
    }

    public int getTypeOf(int columnIndex) throws SQLiteException {
        checkRow();
        return columnType(this.preparedStatement.getStatementHandle(), columnIndex);
    }

    public boolean next() throws SQLiteException {
        int res = this.preparedStatement.step(this.preparedStatement.getStatementHandle());
        if (res == -1) {
            int repeatCount = 6;
            while (true) {
                int repeatCount2 = repeatCount - 1;
                if (repeatCount == 0) {
                    break;
                }
                try {
                    FileLog.m609e("tmessages", "sqlite busy, waiting...");
                    Thread.sleep(500);
                    res = this.preparedStatement.step();
                    if (res == 0) {
                        break;
                    }
                    repeatCount = repeatCount2;
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                    repeatCount = repeatCount2;
                }
            }
            if (res == -1) {
                throw new SQLiteException("sqlite busy");
            }
        }
        this.inRow = res == 0;
        return this.inRow;
    }

    public int getStatementHandle() {
        return this.preparedStatement.getStatementHandle();
    }

    public void dispose() {
        this.preparedStatement.dispose();
    }

    void checkRow() throws SQLiteException {
        if (!this.inRow) {
            throw new SQLiteException("You must call next before");
        }
    }
}
