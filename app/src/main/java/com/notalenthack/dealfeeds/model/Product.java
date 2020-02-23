package com.notalenthack.dealfeeds.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.notalenthack.dealfeeds.common.ItemsContract;

public class Product implements Parcelable {
	private String title 		= "";
	private String link 		= "";
	private String date 		= "";
	private String description 	= "";
	private String vendor 		= "";
	private String imageLink 	= "";
	private String name 		= "";
    private int category 		= ProductCategories.CAT_OTHER; // Defaults to other
    private float price 		= 0;
	private long id;
    private int status          = ItemsContract.STATUS_UNKNOWN;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(title);
        parcel.writeString(link);
        parcel.writeString(date);
        parcel.writeString(description);
        parcel.writeString(vendor);
        parcel.writeString(imageLink);
        parcel.writeString(name);
        parcel.writeInt(category);
        parcel.writeInt(status);
        parcel.writeFloat(price);
        parcel.writeLong(id);
    }

    public void readFromParcel(Parcel parcel) {
        title = parcel.readString();
        link = parcel.readString();
        date = parcel.readString();
        description = parcel.readString();
        vendor = parcel.readString();
        imageLink = parcel.readString();
        name = parcel.readString();
        category = parcel.readInt();
        status = parcel.readInt();
        price = parcel.readFloat();
        id = parcel.readLong();
    }

    public static final Parcelable.Creator<Product> CREATOR = new Parcelable.ClassLoaderCreator<Product>() {
        @Override
        public Product createFromParcel(Parcel parcel, ClassLoader classLoader) {
            return new Product(parcel);
        }

        @Override
        public Product createFromParcel(Parcel parcel) {
            return createFromParcel(parcel, ClassLoader.getSystemClassLoader());
        }

        @Override
        public Product[] newArray(int i) {
            return new Product[i];
        }
    };

    // Create a product from a database cursor
    public Product(Cursor cursor) {
        setTitle(cursor.getString(cursor.getColumnIndex(ItemsContract.COLUMN_TITLE)));
        setLink(cursor.getString(cursor.getColumnIndex(ItemsContract.COLUMN_LINK)));
        setDescription(cursor.getString(cursor.getColumnIndex(ItemsContract.COLUMN_DESCRIPTION)));
        setId(cursor.getLong(cursor.getColumnIndex(ItemsContract.COLUMN_ID)));
        setDate(dateLongValueToString(cursor.getLong(cursor.getColumnIndex(ItemsContract.COLUMN_DATE))));
        setCategory(cursor.getInt(cursor.getColumnIndex(ItemsContract.COLUMN_CATEGORY)));
        setStatus(cursor.getInt(cursor.getColumnIndex(ItemsContract.COLUMN_STATUS)));
        setName(cursor.getString(cursor.getColumnIndex(ItemsContract.COLUMN_NAME)));
        setImageLink(cursor.getString(cursor.getColumnIndex(ItemsContract.COLUMN_IMAGE_LINK)));
        setPrice(Float.parseFloat(cursor.getString(cursor.getColumnIndex(ItemsContract.COLUMN_PRICE))));
        try {
            setPrice(Float.parseFloat(cursor.getString(cursor.getColumnIndex(ItemsContract.COLUMN_PRICE))));
        } catch (NumberFormatException ex) {
            setPrice(0);
        }
    }

    public Product() {
    }

    public Product(Parcel parcel) {
        readFromParcel(parcel);
    }

    public static String dateLongValueToString( long date ) {
        /***
         "yyyy.MM.dd G 'at' HH:mm:ss z"	        2001.07.04 AD at 12:08:56 PDT
         "EEE, MMM d, ''yy"	                    Wed, Jul 4, '01
         "h:mm a"	                            12:08 PM
         "hh 'o''clock' a, zzzz"	            12 o'clock PM, Pacific Daylight Time
         "K:mm a, z"	                        0:08 PM, PDT
         "yyyyy.MMMMM.dd GGG hh:mm aaa"	        02001.July.04 AD 12:08 PM
         "EEE, d MMM yyyy HH:mm:ss Z"	        Wed, 4 Jul 2001 12:08:56 -0700
         "yyMMddHHmmssZ"	                    010704120856-0700
         "yyyy-MM-dd'T'HH:mm:ss.SSSZ"	        2001-07-04T12:08:56.235-0700
         */

        // SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM.dd yyyy, hh:mm:ss a z", Locale.US);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(date));
    }
	
	public long getId() {
		return id;
	}
	
	public void setId(long value) {
		id = value;
	}
	
	public void setDate(String value) {
		date = value;
	}

	public void setLink(String value) {
		link = value;
	}

	public void setTitle(String value) {
		title = value;
		setCategory(ProductCategories.getCatIdFromTitle(value));
	}

	public void setDescription(String value) {
		description = value;
	}
	
	public void setVendor(String value) {
		vendor = value;
	}
	
	public void setCategory(int value) {
		category = value;
	}

    public void setStatus(int value) {
        status = value;
    }

	public void setImageLink(String value) {
		imageLink = value;
	}

	public void setName(String value) {
		name = value;
	}
	
	public void setPrice(float value) {
		price = value;
	}
	
	public String getTitle() {
		return title;
	}

	public String getLink() {
		return link;
	}

	public String getDate() {
		return date;
	}

	public String getDescription() {
		return description;
	}

	public String getVendor() {
		return vendor;
	}

	public int getCategory() {
		return category;
	}

    public int getStatus() {
        return status;
    }

	public String getImageLink() {
		return imageLink;
	}

	public float getPrice() {
		return price;
	}
	
	public String getPriceStr() {
		return String.format(Locale.getDefault(),"%.02f", price);
	}

	public String getName() {
		return name;
	}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id ");
        sb.append(this.id);
        sb.append("title: ");
        sb.append(this.title);
        sb.append(" link: ");
        sb.append(this.link);
        sb.append(" imageLink: ");
        sb.append(this.imageLink);
        sb.append(" price: ");
        sb.append(this.price);
        return sb.toString();
    }
}
