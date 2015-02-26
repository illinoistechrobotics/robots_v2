package org.illinoistechrobotics.jaguar;

public class CAN {
	public static final int DEVTYPE_SYS       = 0;
	public static final int DEVTYPE_MOTORCTRL = 2;
	public static final int MANUFACTURER_SYS  = 0;
	public static final int MANUFACTURER_TI   = 2;

	public static final int START_OF_FRAME = 0xff;
	public static final int ENCODE_BYTE_A  = 0xfe;
	public static final int ENCODE_BYTE_B  = 0xfd;

	public static final int HEADER_SIZE    = 6;
	public static final int CAN_ID_SIZE    = 4;
	public static final int MAX_DATA_BYTES = 8;
	public static final int MAX_MSG_BYTES  = 22;

	// API Classes
	public static final int API_SYS      = 0;
	public static final int API_VOLTAGE  = 0;
	public static final int API_SPEED    = 1;
	public static final int API_VOLTCOMP = 2;
	public static final int API_POSITION = 3;
	public static final int API_CURRENT  = 4;
	public static final int API_STATUS   = 5;
	public static final int API_CONFIG   = 7;
	public static final int API_ACK      = 8;

	// System Control Interface
	public static final int SYS_HALT        = 0;
	public static final int SYS_RESET       = 1;
	public static final int SYS_ASSIGN      = 2;
	public static final int SYS_QUERY       = 3;
	public static final int SYS_HEARTBEAT   = 5;
	public static final int SYS_SYNC_UPDATE = 6;
	public static final int SYS_FW_UPDATE   = 7;
	public static final int SYS_FW_VER      = 8;
	public static final int SYS_ENUMERATION = 9;
	public static final int SYS_RESUME      = 10;

	// Voltage Control Interface
	public static final int VOLTAGE_ENABLE  = 0;
	public static final int VOLTAGE_DISABLE = 1;
	public static final int VOLTAGE_SET     = 2;
	public static final int VOLTAGE_RAMP    = 3;

	// Speed Control Interface
	public static final int SPEED_ENABLE  = 0;
	public static final int SPEED_DISABLE = 1;
	public static final int SPEED_SET     = 2;
	public static final int SPEED_P       = 3;
	public static final int SPEED_I       = 4;
	public static final int SPEED_D       = 5;
	public static final int SPEED_REF     = 6;

	public static final int SPEED_ENCODER_1CH     = 0;
	public static final int SPEED_ENCODER_1CH_INV = 2;
	public static final int SPEED_ENCODER_QUAD    = 3;

	// Voltage Compensation Control Interface
	public static final int VOLTCOMP_ENABLE  = 0;
	public static final int VOLTCOMP_DISABLE = 1;
	public static final int VOLTCOMP_SET     = 2;
	public static final int VOLTCOMP_RAMP    = 3;
	public static final int VOLTCOMP_RATE    = 4;

	// Position Control Interface
	public static final int POSITION_ENABLE  = 0;
	public static final int POSITION_DISABLE = 1;
	public static final int POSITION_SET     = 2;
	public static final int POSITION_P       = 3;
	public static final int POSITION_I       = 4;
	public static final int POSITION_D       = 5;
	public static final int POSITION_REF     = 6;

	public static final int POSITION_ENCODER       = 0;
	public static final int POSITION_POTENTIOMETER = 1;

	// Current Control Interface
	public static final int CURRENT_ENABLE  = 0;
	public static final int CURRENT_DISABLE = 1;
	public static final int CURRENT_SET     = 2;
	public static final int CURRENT_P       = 3;
	public static final int CURRENT_I       = 4;
	public static final int CURRENT_D       = 5;

	// Motor Control Status
	public static final int STATUS_OUTPUT_PERCENT = 0;
	public static final int STATUS_BUS_VOLTAGE    = 1;
	public static final int STATUS_CURRENT        = 2;
	public static final int STATUS_TEMPERATURE    = 3;
	public static final int STATUS_POSITION       = 4;
	public static final int STATUS_SPEED          = 5;
	public static final int STATUS_LIMIT          = 6;
	public static final int STATUS_FAULT          = 7;
	public static final int STATUS_POWER          = 8;
	public static final int STATUS_MODE           = 9;
	public static final int STATUS_OUTPUT_VOLTS   = 10;

	public static final int FORWARD_LIMIT_REACHED = 0;
	public static final int REVERSE_LIMIT_REACHED = 1;

	public static final int CURRENT_FAULT     = 0;
	public static final int TEMPERATURE_FAULT = 1;
	public static final int BUS_VOLTAGE_FAULT = 2;

	public static final int STATUS_MODE_VOLTAGE  = 0;
	public static final int STATUS_MODE_CURRENT  = 1;
	public static final int STATUS_MODE_SPEED    = 2;
	public static final int STATUS_MODE_POSITION = 3;
	public static final int STATUS_MODE_VOLTCOMP = 4;

	// Motor Control Configuration
	public static final int CONFIG_BRUSHES       = 0;
	public static final int CONFIG_ENCODER_LINES = 1;
	public static final int CONFIG_POT_TURNS     = 2;
	public static final int CONFIG_BREAK_COAST   = 3;
	public static final int CONFIG_LIMIT_MODE    = 4;
	public static final int CONFIG_FORWARD_LIMIT = 5;
	public static final int CONFIG_REVERSE_LIMIT = 6;
	public static final int CONFIG_MAX_VOLTAGE   = 7;
	public static final int CONFIG_FAULT_TIME    = 8;
}
