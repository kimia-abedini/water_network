from datetime import datetime
from django.db import models
from django.contrib.auth.models import User
from PIL import Image
from django.contrib.gis.db import models as gis_models
from django.utils import timezone


class Role(models.Model):
    title = models.CharField(max_length=255)
    description = models.TextField(blank=True, null=True)
    created_at = models.DateTimeField(blank=True, null=True)
    updated_at = models.DateTimeField(blank=True, null=True)

    class Meta:
        managed = False
        db_table = 'role'


# Extending User Model Using a One-To-One Link
class Profile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    address = models.TextField(blank=True, null=True)
    role = models.ForeignKey(Role, models.DO_NOTHING, default=1)
    phone_number = models.CharField(max_length=15, blank=True, null=True)
    # created_at = models.DateTimeField(blank=True, auto_now_add=True)
    # updated_at = models.DateTimeField(blank=True, auto_now=True)
    #
    # class Meta:
    #     managed = False
    #     db_table = 'users'

    def __str__(self):
        return self.user.username

# class UsersReport(models.Model):
#     user_coordinate = models.TextField(blank=True, null=True)
#     report_type = models.CharField(max_length=255, blank=True, null=True)
#     description = models.TextField(blank=True, null=True)
#     photo = models.ImageField(default='default.jpg', upload_to='report_images')
#     report_date = models.DateField(blank=True, null=True)
#     user_id = models.ForeignKey(User, models.DO_NOTHING, blank=True, null=True)
#
#     # class Meta:
#     #     managed = False
#     #     db_table = 'users_usersreport'
#
#     def save(self, *args, **kwargs):
#         super().save(*args, **kwargs)
#
#         img = Image.open(self.photo.path)
#
#         if img.height > 100 or img.width > 100:
#             new_img = (100, 100)
#             img.thumbnail(new_img)
#             img.save(self.photo.path)



class Report(models.Model):
    user_coordinate = gis_models.PointField(blank=True, null=True)
    report_type = models.CharField(max_length=255, blank=True, null=True)
    description = models.TextField(blank=True, null=True)
    photo = models.ImageField(default='default.jpg', upload_to='report_images')
    report_date = models.DateTimeField(default=datetime.now)
    user_id = models.ForeignKey(User, models.DO_NOTHING, blank=True, null=True)

    def save(self, *args, **kwargs):
        if not self.pk:  # If object is being created
            self.report_date = timezone.now()  # Set report_date to current time
        super().save(*args, **kwargs)

    # class Meta:
    #     managed = False
    #     db_table = 'users_usersreport'

    # def save(self, *args, **kwargs):
    #     super().save(*args, **kwargs)

        # img = Image.open(self.photo.path)

        # if img.height > 100 or img.width > 100:
        #     new_img = (100, 100)
        #     img.thumbnail(new_img)
        #     img.save(self.photo.path)


class JunctionManholeElements(models.Model):
    pozzetti = models.ForeignKey('Pozzetti', models.DO_NOTHING, to_field='pozzetti_id')
    element_type = models.CharField(max_length=255, blank=True, null=True)
    installation_date = models.DateField(blank=True, null=True)
    valve_state = models.CharField(max_length=255, blank=True, null=True)
    pump_state = models.CharField(max_length=255, blank=True, null=True)

    class Meta:
        managed = False
        db_table = 'junction_manhole_elements'


class Pozzetti(models.Model):
    gid = models.AutoField(primary_key=True)
    id = models.FloatField(blank=True, null=True)
    grado = models.FloatField(blank=True, null=True)
    geom = gis_models.PointField()
    pozzetti_id = models.IntegerField(unique=True, blank=True, null=True)

    class Meta:
        managed = False
        db_table = 'pozzetti'
